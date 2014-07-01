package org.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.spi.cluster.Action;
import io.vertx.core.spi.cluster.AsyncMap;
import io.vertx.core.spi.cluster.VertxSPI;
import org.infinispan.Cache;

public class InfinispanAsyncMap<K, V> implements AsyncMap<K, V> {

    private VertxSPI vertx;
    private Cache<K, V> cache;

    public InfinispanAsyncMap(VertxSPI vertx, Cache<K, V> cache) {
        this.vertx = vertx;
        this.cache = cache;
    }

    @Override
    public void get(final K k, Handler<AsyncResult<V>> asyncResultHandler) {
        System.out.println("GET K");
        vertx.executeBlocking(new Action<V>() {
            public V perform() {
                return cache.get(k);
            }
        }, asyncResultHandler);
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
        System.out.println("PUT K");
        vertx.executeBlocking(new Action<Void>() {
            public Void perform() {
                cache.put(k, v);
                return null;
            }
        }, completionHandler);
    }

    @Override
    public void remove(final K k, Handler<AsyncResult<Void>> completionHandler) {
        System.out.println("REMOVE K");
        vertx.executeBlocking(new Action<Void>() {
            public Void perform() {
                cache.remove(k);
                return null;
            }
        }, completionHandler);
    }
}
