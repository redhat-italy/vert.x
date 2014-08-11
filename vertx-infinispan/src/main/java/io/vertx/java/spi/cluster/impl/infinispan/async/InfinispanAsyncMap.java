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

package io.vertx.java.spi.cluster.impl.infinispan.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.HandlerHelper;
import org.infinispan.Cache;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.CacheAsyncWrapper;

public class InfinispanAsyncMap<K, V> implements AsyncMap<K, V> {

    private final CacheAsyncWrapper<K, V> wrapper;

    public InfinispanAsyncMap(Cache<K, V> cache) {
        this.wrapper = new CacheAsyncWrapper<>(cache);
    }

    @Override
    public void get(final K k, Handler<AsyncResult<V>> handler) {
        HandlerHelper<V> helper = new HandlerHelper<>(handler);

        wrapper
                .get(k,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<Void>> handler) {
        HandlerHelper<Void> helper = new HandlerHelper<>(handler);

        wrapper
                .put(k, v,
                        (value) -> helper.success(null),
                        helper::error
                );
    }

    @Override
    public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> handler) {
        HandlerHelper<V> helper = new HandlerHelper<>(handler);

        wrapper
                .putIfAbsent(k, v,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void remove(K k, Handler<AsyncResult<V>> handler) {
        HandlerHelper<V> helper = new HandlerHelper<>(handler);

        wrapper
                .remove(k,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> handler) {
        HandlerHelper<Boolean> helper = new HandlerHelper<>(handler);

        wrapper
                .removeIfPresent(k,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void replace(K k, V v, Handler<AsyncResult<V>> handler) {
        HandlerHelper<V> helper = new HandlerHelper<>(handler);

        wrapper
                .replace(k, v,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> handler) {
        HandlerHelper<Boolean> helper = new HandlerHelper<>(handler);

        wrapper
                .replace(k, oldValue, newValue,
                        helper::success,
                        helper::error
                );
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        HandlerHelper<Void> helper = new HandlerHelper<>(handler);

        wrapper
                .clear(
                        helper::success,
                        helper::error
                );
    }

}
