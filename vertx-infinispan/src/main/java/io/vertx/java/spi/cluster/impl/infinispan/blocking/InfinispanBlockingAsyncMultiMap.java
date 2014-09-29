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

package io.vertx.java.spi.cluster.impl.infinispan.blocking;

import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSetImpl;
import org.infinispan.Cache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

public class InfinispanBlockingAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(InfinispanBlockingAsyncMultiMap.class);

    private VertxSPI vertx;
    private final Cache<K, ImmutableChoosableSet<V>> cache;

    public InfinispanBlockingAsyncMultiMap(VertxSPI vertx, Cache<K, ImmutableChoosableSet<V>> cache) {
        this.vertx = vertx;
        this.cache = cache;
    }

    @Override
    public void get(final K k, Handler<AsyncResult<ChoosableIterable<V>>> completionHandler) {
        vertx.executeBlocking(() -> cache.get(k), completionHandler);
    }

    @Override
    public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
        vertx.executeBlocking(() -> {
            ImmutableChoosableSet<V> oldValue = cache.get(k);
            if (oldValue != null) {
                if (cache.replace(k, oldValue, oldValue.remove(v))) {
                    log.debug("Value changed during update");
                    return false;
                }
            }
            return true;
        }, completionHandler);
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(() -> {
            ImmutableChoosableSet<V> oldValue = cache.get(k);
            if (oldValue != null) {
                if (!cache.replace(k, oldValue, oldValue.add(v))) {
                    log.error(String.format("Value changed during update for key [%s], retry []", k.toString()));
                    throw new RuntimeException("Value changed during update");
                }
            } else {
                cache.put(k, new ImmutableChoosableSetImpl<V>(v));
            }
            return null;
        }, completionHandler);
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        //This should be a distributed execute on primary(local) key
        throw new UnsupportedOperationException("not yet implemented");
    }
}
