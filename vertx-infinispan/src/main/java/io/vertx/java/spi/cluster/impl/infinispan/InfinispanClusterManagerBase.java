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
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.*;
import io.vertx.java.spi.cluster.impl.infinispan.listeners.CacheManagerListener;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import io.vertx.java.spi.cluster.impl.infinispan.domain.serializer.ImmutableChoosableSetSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class InfinispanClusterManagerBase implements ClusterManager {

    private final static Logger LOG = LoggerFactory.getLogger(InfinispanClusterManagerBase.class);

    private final Configuration syncConfiguration;
    private final Configuration asyncConfiguration;

    private EmbeddedCacheManager cacheManager;
    private VertxSPI vertxSPI;
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

    protected final Configuration getSyncConfiguration() {
        return syncConfiguration;
    }

    protected final Configuration getAsyncConfiguration() {
        return asyncConfiguration;
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
        ArrayList<String> nodes = new ArrayList<>();
        for (Address address : cacheManager.getMembers()) {
            nodes.add(address.toString());
        }
        return nodes;
    }

    @Override
    public final void nodeListener(NodeListener listener) {
        this.cacheManager.addListener(new CacheManagerListener(listener));
    }

    @Override
    public final void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> resultHandler) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public final void getCounter(String name, Handler<AsyncResult<Counter>> resultHandler) {
        throw new UnsupportedOperationException("Not yet implemented.");
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
            active = true;
            return null;
        }, handler);
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
