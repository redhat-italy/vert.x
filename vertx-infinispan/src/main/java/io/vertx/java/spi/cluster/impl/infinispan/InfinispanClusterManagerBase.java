/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.spi.cluster.Action;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.infinispan.domain.InfinispanCounterImpl;
import io.vertx.java.spi.cluster.impl.infinispan.domain.serializer.ImmutableChoosableSetSerializer;
import io.vertx.java.spi.cluster.impl.infinispan.listeners.CacheManagerListener;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.*;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public abstract class InfinispanClusterManagerBase implements ClusterManager {

  private final static Logger LOG = LoggerFactory.getLogger(InfinispanClusterManagerBase.class);
  public static final String VERTX_COUNTER_CHANNEL = "__vertx__counter_channel";
  public static final String VERTX_LOCK_CHANNEL = "__vertx__lock_channel";

  private final Configuration syncConfiguration;
  private final Configuration asyncConfiguration;

  private EmbeddedCacheManager cacheManager;
  private VertxSPI vertxSPI;
  private CounterService counterService;
  private LockService lockService;

  private boolean active = false;

  public InfinispanClusterManagerBase() {
    this.syncConfiguration = new ConfigurationBuilder()
        .clustering().cacheMode(CacheMode.DIST_SYNC)
        .hash().numOwners(2)
        .build();
    this.asyncConfiguration = new ConfigurationBuilder()
        .clustering().cacheMode(CacheMode.DIST_ASYNC)
        .hash().numOwners(2)
        .build();
  }

  protected final VertxSPI getVertxSPI() {
    return vertxSPI;
  }

  protected final EmbeddedCacheManager getCacheManager() {
    return cacheManager;
  }

  protected <T> void execute(Action<T> action, Handler<AsyncResult<T>> handler) {
    vertxSPI.executeBlocking(action, handler);
  }

  @Override
  public final void setVertx(VertxSPI vertxSPI) {
    this.vertxSPI = vertxSPI;
  }

  @Override
  public final String getNodeID() {
    return cacheManager.getAddress().toString();
  }

  @Override
  public final List<String> getNodes() {
    return Optional
        .ofNullable(cacheManager.getMembers())
        .orElse(Collections.emptyList())
        .stream()
        .map(Address::toString)
        .collect(Collectors.toList());
  }

  @Override
  public final void nodeListener(NodeListener listener) {
    this.cacheManager.addListener(new CacheManagerListener(listener));
  }

  @Override
  public final void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<io.vertx.core.shareddata.Lock>> handler) {
    vertxSPI.executeBlocking(
        () -> {
          try {
            Lock lock = lockService.getLock(name);
            System.out.println(String.format("TRY LOCK on [%s] Thread [%s]", name, Thread.currentThread()));
            if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
              System.out.println(String.format("LOCKED on [%s] Thread [%s]", name, Thread.currentThread()));
              return (io.vertx.core.shareddata.Lock) lock::unlock;
            }
          } catch (InterruptedException e) {
          }
          throw new VertxException("Timed out waiting to get lock " + name);
        },
        handler
    );
  }

  @Override
  public final void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
    vertxSPI.executeBlocking(
        () -> new InfinispanCounterImpl(vertxSPI, counterService.getOrCreateCounter(name, 0L)),
        handler
    );
  }

  @Override
  public final <K, V> Map<K, V> getSyncMap(String name) {
    getCacheManager().defineConfiguration(name, syncConfiguration);
    return getCacheManager().<K, V>getCache(name, true);
  }

  @Override
  public final boolean isActive() {
    return active;
  }

  @Override
  public final void join(Handler<AsyncResult<Void>> handler) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("JOIN [%s]", this.toString()));
    }
    vertxSPI.executeBlocking(() -> {
      if (active) {
        return null;
      }

      GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
          .clusteredDefault()
          .classLoader(GlobalConfiguration.class.getClassLoader())
          .transport().addProperty("configurationFile", "jgroups-udp.xml")
          .globalJmxStatistics().allowDuplicateDomains(true).enable()
          .serialization().addAdvancedExternalizer(new ImmutableChoosableSetSerializer())
          .build();
      cacheManager = new DefaultCacheManager(globalConfiguration, asyncConfiguration);
      cacheManager.start();

      JGroupsTransport transport = (JGroupsTransport) cacheManager.getCache().getAdvancedCache().getRpcManager().getTransport();

      JChannel counterChannel = forkChannel(transport.getChannel(), VERTX_COUNTER_CHANNEL, cacheManager.getAddress().toString(), new COUNTER());
      counterService = new CounterService(counterChannel);

      JChannel lockChannel = forkChannel(transport.getChannel(), VERTX_LOCK_CHANNEL, cacheManager.getAddress().toString(), new SEQUENCER(), new PEER_LOCK());
      lockService = new LockService(lockChannel);

      active = true;
      return null;
    }, handler);
  }

  private ForkChannel forkChannel(Channel mainChannel, String forkStackId, String channelId, Protocol... protocols) {
    try {
      ForkChannel forkChannel = new ForkChannel(mainChannel, forkStackId, channelId, true, ProtocolStack.ABOVE, FRAG2.class, protocols);
      forkChannel.connect("ignored");
      return forkChannel;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public final void leave(Handler<AsyncResult<Void>> handler) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("LEAVE Active[%s] [%s]", active, this.toString()));
    }
    vertxSPI.executeBlocking(() -> {
      if (!active) {
        return null;
      }
      cacheManager.stop();
      cacheManager = null;
      active = false;
      return null;
    }, handler);
  }
}
