package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.FutureListener;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.domain.support.ElementNotFoundInSetException;

import java.util.concurrent.Future;

public class InfinispanAsyncMultiMapBlocking<K, V> implements AsyncMultiMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(InfinispanAsyncMultiMapBlocking.class);

    private VertxSPI vertx;
    private final Cache<K, ImmutableChoosableSet<V>> cache;

    public InfinispanAsyncMultiMapBlocking(VertxSPI vertx, Cache<K, ImmutableChoosableSet<V>> cache) {
        this.vertx = vertx;
        this.cache = cache;
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
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
    public void remove(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(new Action<Void>() {
            @Override
            public Void perform() {
                try {
                    ImmutableChoosableSet<V> oldValue = cache.get(k);
                    cache.replace(k, oldValue, oldValue.remove(v));
                    return null;
                } catch (ElementNotFoundInSetException e) {
                    throw new RuntimeException(e);
                }
            }
        }, completionHandler);
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        //This should be a distributed execute on primary(local) key
        throw new UnsupportedOperationException("not yet implemented");
    }
}
