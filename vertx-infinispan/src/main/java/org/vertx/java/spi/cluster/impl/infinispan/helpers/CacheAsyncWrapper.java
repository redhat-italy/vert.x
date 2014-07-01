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

package org.vertx.java.spi.cluster.impl.infinispan.helpers;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;
import io.vertx.core.spi.cluster.ChoosableIterable;

public class CacheAsyncWrapper<K1, V1 extends ChoosableIterable> {

    private final Cache<K1, V1> cache;


    public CacheAsyncWrapper(Cache<K1, V1> cache) {
        this.cache = cache;
    }

    public FutureListenerHelper<V1> get(K1 k) {
        return perform(cache.getAsync(k));
    }

    public FutureListenerHelper<V1> put(K1 k, V1 v) {
        return perform(cache.putAsync(k, v));
    }

    public FutureListenerHelper<V1> remove(K1 k, V1 v) {
        if(v.isEmpty()) {
            return perform(cache.removeAsync(k));
        } else {
            return perform(cache.putAsync(k, v));
        }
    }

    private <T> FutureListenerHelper<T> perform(NotifyingFuture<T> future) {
        FutureListenerHelper<T> subscriber = new FutureListenerHelper<>();
        future.attachListener(subscriber);
        return subscriber;
    }
}
