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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.MapOptions;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.infinispan.domain.InfinispanCounterImpl;
import io.vertx.java.spi.cluster.impl.infinispan.domain.InfinispanLockImpl;
import io.vertx.java.spi.cluster.impl.infinispan.domain.serializer.ImmutableChoosableSetSerializer;
import io.vertx.java.spi.cluster.impl.infinispan.listeners.CacheManagerListener;
import io.vertx.java.spi.cluster.impl.jgroups.protocols.VERTX_LOCK;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.COUNTER;
import org.jgroups.protocols.FRAG2;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class InfinispanClusterManagerBase implements ClusterManager {

  private final static Logger log = LoggerFactory.getLogger(InfinispanClusterManagerBase.class);
  public static final String VERTX_COUNTER_CHANNEL = "__vertx__counter_channel";
  public static final String VERTX_LOCK_CHANNEL = "__vertx__lock_channel";

  private DefaultCacheManager cacheManager;
  private VertxSPI vertxSPI;
  private CounterService counterService;
  private LockService lockService;

  private boolean active = false;

  private JChannel lockChannel;
  private JChannel counterChannel;

  protected abstract <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(Cache<K, ImmutableChoosableSet<V>> cache);

  protected abstract <K, V> AsyncMap<K, V> getAsyncMap(Cache<K, V> cache);

  protected final VertxSPI getVertx() {
    return vertxSPI;
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
    if (log.isDebugEnabled()) {
      log.debug(String.format("Add node listener [%s]", listener.toString()));
    }
    this.cacheManager.addListener(new CacheManagerListener(listener));
  }

  @Override
  public final void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<io.vertx.core.shareddata.Lock>> handler) {
    vertxSPI.executeBlocking(
        () -> {
          InfinispanLockImpl infinispanLock = new InfinispanLockImpl(lockService, name);
          infinispanLock.acquire(timeout);
          return infinispanLock;
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
    return cacheManager.<K, V>getCache(name, true);
  }

  @Override
  public final <K, V> void getAsyncMultiMap(String name, MapOptions options, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
    vertxSPI.executeBlocking(
        () -> this.getAsyncMultiMap(cacheManager.<K, ImmutableChoosableSet<V>>getCache(name, true)),
        handler);
  }

  @Override
  public final <K, V> void getAsyncMap(String name, MapOptions options, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
    vertxSPI.executeBlocking(
        () -> this.getAsyncMap(cacheManager.<K, V>getCache(name, true)),
        handler);
  }

  @Override
  public final boolean isActive() {
    return active;
  }

  @Override
  public final void leave(Handler<AsyncResult<Void>> handler) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("LEAVE Active[%s] [%s]", active, this.toString()));
    }
    vertxSPI.executeBlocking(() -> {
      if (!active) {
        return null;
      }
      active = false;

      counterChannel.close();
      lockChannel.close();

      cacheManager.stop();
      cacheManager = null;
      return null;
    }, handler);
  }

  @Override
  public final void join(Handler<AsyncResult<Void>> handler) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("Join to the cluster [%s]", this.toString()));
    }
    vertxSPI.executeBlocking(() -> {
      if (active) {
        return null;
      }
      active = true;

      GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
          .clusteredDefault()
          .transport().addProperty("configurationFile", "jgroups-udp.xml")
          .globalJmxStatistics().allowDuplicateDomains(true).disable()
          .serialization().addAdvancedExternalizer(new ImmutableChoosableSetSerializer())
          .build();

      Configuration syncConfiguration = new ConfigurationBuilder()
          .clustering().cacheMode(CacheMode.DIST_SYNC)
          .hash().numOwners(2)
          .build();

      cacheManager = new DefaultCacheManager(globalConfiguration, syncConfiguration);
      cacheManager.start();

      JGroupsTransport transport = (JGroupsTransport) cacheManager.getCache().getAdvancedCache().getRpcManager().getTransport();

      lockChannel = forkChannel(transport.getChannel(), VERTX_LOCK_CHANNEL, this.getNodeID(), new VERTX_LOCK());
      lockService = new LockService(lockChannel);

      counterChannel = forkChannel(transport.getChannel(), VERTX_COUNTER_CHANNEL, this.getNodeID(), new COUNTER());
      counterService = new CounterService(counterChannel);

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
}
