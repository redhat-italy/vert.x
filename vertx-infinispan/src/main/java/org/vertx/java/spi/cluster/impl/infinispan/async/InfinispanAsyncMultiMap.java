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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import org.infinispan.Cache;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.helpers.CacheAsyncWrapper;


public class InfinispanAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(InfinispanAsyncMultiMap.class);

    private final CacheAsyncWrapper<K, ImmutableChoosableSet<V>> wrapper;

    public InfinispanAsyncMultiMap(Cache<K, ImmutableChoosableSet<V>> cache) {
        this.wrapper = new CacheAsyncWrapper<>(cache);
    }

    @Override
    public void get(final K k, final Handler<AsyncResult<ChoosableIterable<V>>> handler) {
        FutureResultImpl<ChoosableIterable<V>> result = new FutureResultImpl<>();

        wrapper
                .get(k,
                        (value) -> {
                            result.setResult(value);
                            result.complete();
                            handler.handle(result);
                        },
                        (e) -> {
                            result.setFailure(e);
                            result.failed();
                            handler.handle(result);
                        });
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> handler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .get(k,
                        (value) -> {
                            if (value != null) {
                                wrapper
                                        .put(k, value.add(v),
                                                (set) -> {
                                                    result.setResult(null);
                                                    result.complete();
                                                    handler.handle(result);
                                                },
                                                (e) -> {
                                                    result.setFailure(e);
                                                    result.failed();
                                                    handler.handle(result);
                                                });
                            } else {
                                result.setResult(null);
                                result.complete();
                                handler.handle(result);
                            }
                        },
                        (e) -> {
                            result.setFailure(e);
                            result.failed();
                            handler.handle(result);
                        });
    }

    @Override
    public void remove(final K k, final V v, final Handler<AsyncResult<Void>> handler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .get(k,
                        (value) -> {
                            if (value != null) {
                                if (value.isEmpty()) {
                                    wrapper.remove(k,
                                            (set) -> {
                                                result.setResult(null);
                                                result.complete();
                                                handler.handle(result);
                                            },
                                            (e) -> {
                                                result.setFailure(e);
                                                result.failed();
                                                handler.handle(result);
                                            });
                                } else {
                                    wrapper.put(k, value.remove(v),
                                            (set) -> {
                                                result.setResult(null);
                                                result.complete();
                                                handler.handle(result);
                                            },
                                            (e) -> {
                                                result.setFailure(e);
                                                result.failed();
                                                handler.handle(result);
                                            });
                                }
                            } else {
                                result.setResult(null);
                                result.complete();
                                handler.handle(result);
                            }
                        },
                        (e) -> {
                            result.setFailure(e);
                            result.failed();
                            handler.handle(result);
                        });
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
