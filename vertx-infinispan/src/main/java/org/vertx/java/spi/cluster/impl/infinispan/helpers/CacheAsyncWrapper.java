package org.vertx.java.spi.cluster.impl.infinispan.helpers;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;
import org.vertx.java.core.spi.cluster.ChoosableIterable;

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
