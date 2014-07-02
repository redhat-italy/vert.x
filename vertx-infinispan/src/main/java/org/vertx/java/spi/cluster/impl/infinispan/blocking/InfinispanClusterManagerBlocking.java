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

package org.vertx.java.spi.cluster.impl.infinispan.blocking;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.*;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.vertx.java.spi.cluster.impl.infinispan.InfinispanClusterManagerBase;
import org.vertx.java.spi.cluster.impl.infinispan.async.InfinispanAsyncMap;
import org.vertx.java.spi.cluster.impl.infinispan.async.InfinispanAsyncMultiMap;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.domain.serializer.ImmutableChoosableSetSerializer;
import org.vertx.java.spi.cluster.impl.infinispan.listeners.CacheManagerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfinispanClusterManagerBlocking extends InfinispanClusterManagerBase {

    @Override
    public <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String name) {
        Cache<K, ImmutableChoosableSet<V>> cache = getCacheManager().<K, ImmutableChoosableSet<V>>getCache(name, true);
        return new InfinispanAsyncMultiMapBlocking<>(getVertxSPI(), cache);
    }

    @Override
    public <K, V> AsyncMap<K, V> getAsyncMap(String name) {
        Cache<K, V> cache = getCacheManager().<K, V>getCache(name, true);
        return new InfinispanAsyncMapBlocking<>(getVertxSPI(), cache);
    }

    @Override
    public <K, V> Map<K, V> getSyncMap(String name) {
        getCacheManager().defineConfiguration(name, getSyncConfiguration());
        return getCacheManager().<K, V>getCache(name, true);
    }

}
