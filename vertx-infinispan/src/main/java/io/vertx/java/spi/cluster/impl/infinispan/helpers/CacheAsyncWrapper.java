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

package io.vertx.java.spi.cluster.impl.infinispan.helpers;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;

import java.util.function.Consumer;

public class CacheAsyncWrapper<K1, V1> {

    private final Cache<K1, V1> cache;

    public CacheAsyncWrapper(Cache<K1, V1> cache) {
        this.cache = cache;
    }

    public FutureListenerHelper<Void> clear(Consumer<Void> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.clearAsync(), onSuccess, onError);
    }

    public FutureListenerHelper<V1> get(K1 k, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.getAsync(k), onSuccess, onError);
    }

    public FutureListenerHelper<V1> put(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.putAsync(k, v), onSuccess, onError);
    }

    public FutureListenerHelper<V1> putIfAbsent(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.putIfAbsentAsync(k, v), onSuccess, onError);
    }

    public FutureListenerHelper<V1> replace(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.replaceAsync(k, v), onSuccess, onError);
    }

    public FutureListenerHelper<Boolean> replace(K1 k, V1 oldValue, V1 newValue, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.replaceAsync(k, oldValue, newValue), onSuccess, onError);
    }

    public FutureListenerHelper<V1> remove(K1 k, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.removeAsync(k), onSuccess, onError);
    }

    public FutureListenerHelper<V1> removeIfPresent(K1 k, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        return perform(cache.removeAsync(k), (v) -> onSuccess.accept(v != null), onError);
    }

    private static <V> FutureListenerHelper<V> perform(NotifyingFuture<V> future, Consumer<V> onSuccess, Consumer<Throwable> onError) {
        FutureListenerHelper<V> subscriber = new FutureListenerHelper<V>(onSuccess, onError);
        future.attachListener(subscriber);
        return subscriber;
    }
}
