package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;

public class InfinispanAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private VertxSPI vertx;
    private final Cache<K, ImmutableChoosableSet<V>> cache;

    public InfinispanAsyncMultiMap(VertxSPI vertx, Cache<K, ImmutableChoosableSet<V>> cache) {
        this.vertx = vertx;
        this.cache = cache;
    }

    @Override
    public void add(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(new Action<Void>() {
            @Override
            public Void perform() {
                ImmutableChoosableSet<V> oldValue = cache.get(k);
                cache.replace(k, oldValue, oldValue.add(v));
                return null;
            }
        }, completionHandler);
    }

    @Override
    public void get(final K k, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler) {
        vertx.executeBlocking(new Action<ChoosableIterable<V>>() {
            @Override
            public ChoosableIterable<V> perform() {
                return cache.get(k);
            }
        }, asyncResultHandler);
    }

    @Override
    public void remove(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
