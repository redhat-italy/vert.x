package org.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.spi.cluster.Action;
import io.vertx.core.spi.cluster.VertxSPI;
import org.infinispan.Cache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.spi.cluster.AsyncMap;

public class InfinispanAsyncMapBlocking<K, V> implements AsyncMap<K, V> {

    private VertxSPI vertx;
    private Cache<K, V> cache;

    public InfinispanAsyncMapBlocking(VertxSPI vertx, Cache<K, V> cache) {
        this.vertx = vertx;
        this.cache = cache;
    }

    @Override
    public void get(final K k, Handler<AsyncResult<V>> asyncResultHandler) {
        vertx.executeBlocking(new Action<V>() {
            public V perform() {
                return cache.get(k);
            }
        }, asyncResultHandler);
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(new Action<Void>() {
            public Void perform() {
                cache.put(k, v);
                return null;
            }
        }, completionHandler);
    }

    @Override
    public void remove(final K k, Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(new Action<Void>() {
            public Void perform() {
                cache.remove(k);
                return null;
            }
        }, completionHandler);
    }
}
