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

package org.vertx.java.spi.cluster.impl.infinispan;

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
        FutureResultImpl<V> result = new FutureResultImpl<>();

        wrapper
                .get(k)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    handler.handle(result);
                })
                .onSuccess((value) -> {
                    result.setResult(value);
                    result.complete();
                    handler.handle(result);
                });
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<Void>> handler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .put(k, v)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    handler.handle(result);
                })
                .onSuccess((value) -> {
                    result.setResult(null);
                    result.complete();
                    handler.handle(result);
                });
    }

    @Override
    public void remove(final K k, Handler<AsyncResult<Void>> handler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .remove(k)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    handler.handle(result);
                })
                .onSuccess((value) -> {
                    result.setResult(null);
                    result.complete();
                    handler.handle(result);
                });
    }
}
