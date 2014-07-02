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

package org.vertx.java.spi.cluster.impl.infinispan.async;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMap;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import org.infinispan.Cache;
import org.vertx.java.spi.cluster.impl.infinispan.InfinispanClusterManagerBase;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;

import java.util.Map;

public class InfinispanClusterManagerAsync extends InfinispanClusterManagerBase {

    @Override
    public <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String name) {
        Cache<K, ImmutableChoosableSet<V>> cache = getCacheManager().<K, ImmutableChoosableSet<V>>getCache(name, true);
        return new InfinispanAsyncMultiMap<>(cache);
    }

    @Override
    public <K, V> AsyncMap<K, V> getAsyncMap(String name) {
        Cache<K, V> cache = getCacheManager().<K, V>getCache(name, true);
        return new InfinispanAsyncMap<>(cache);
    }

    @Override
    public <K, V> Map<K, V> getSyncMap(String name) {
        getCacheManager().defineConfiguration(name, getSyncConfiguration());
        return getCacheManager().<K, V>getCache(name, true);
    }
}
