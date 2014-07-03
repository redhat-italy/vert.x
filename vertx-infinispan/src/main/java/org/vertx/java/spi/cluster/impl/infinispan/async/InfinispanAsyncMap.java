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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.FutureResultImpl;
import io.vertx.core.spi.cluster.AsyncMap;
import org.infinispan.Cache;
import org.vertx.java.spi.cluster.impl.infinispan.helpers.CacheAsyncWrapper;

public class InfinispanAsyncMap<K, V> implements AsyncMap<K, V> {

    private final CacheAsyncWrapper<K, V> wrapper;

    public InfinispanAsyncMap(Cache<K, V> cache) {
        this.wrapper = new CacheAsyncWrapper<>(cache);
    }

    @Override
    public void get(final K k, Handler<AsyncResult<V>> handler) {
        wrapper
                .get(k,
                        (value) -> handler.handle(new FutureResultImpl<>(value)),
                        (e) -> handler.handle(new FutureResultImpl<>(e))
                );
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<Void>> handler) {
        wrapper
                .put(k, v,
                        (value) -> handler.handle(new FutureResultImpl<>((Void) null)),
                        (e) -> handler.handle(new FutureResultImpl<>(e))
                );
    }

    @Override
    public void remove(final K k, Handler<AsyncResult<Void>> handler) {
        wrapper
                .remove(k,
                        (value) -> handler.handle(new FutureResultImpl<>((Void) null)),
                        (e) -> handler.handle(new FutureResultImpl<>(e))
                );
    }
}
