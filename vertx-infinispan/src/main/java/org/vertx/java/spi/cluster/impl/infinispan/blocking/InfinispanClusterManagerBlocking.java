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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.MapOptions;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import org.infinispan.Cache;
import org.vertx.java.spi.cluster.impl.infinispan.InfinispanClusterManagerBase;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;

public class InfinispanClusterManagerBlocking extends InfinispanClusterManagerBase {

    @Override
    public <K, V> void getAsyncMultiMap(String name, MapOptions options, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
        getVertxSPI().executeBlocking(() -> {
            Cache<K, ImmutableChoosableSet<V>> cache = getCacheManager().<K, ImmutableChoosableSet<V>>getCache(name, true);
            return new InfinispanAsyncMultiMapBlocking<>(getVertxSPI(), cache);
        }, handler);
    }

    @Override
    public <K, V> void getAsyncMap(String name, MapOptions options, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
        getVertxSPI().executeBlocking(() -> {
            Cache<K, V> cache = getCacheManager().<K, V>getCache(name, true);
            return new InfinispanAsyncMapBlocking<>(getVertxSPI(), cache);
        }, handler);
    }

}
