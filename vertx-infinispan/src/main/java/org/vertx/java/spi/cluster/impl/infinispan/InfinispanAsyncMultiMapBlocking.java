package org.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.spi.cluster.Action;
import io.vertx.core.spi.cluster.VertxSPI;
import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.FutureListener;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSetImpl;
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
    public void get(final K k, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler) {
        vertx.executeBlocking(() -> cache.get(k), asyncResultHandler);
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(() -> {
            ImmutableChoosableSet<V> oldValue = cache.get(k);
            if (oldValue != null) {
                if (!cache.replace(k, oldValue, oldValue.add(v))) {
                    throw new RuntimeException("Value changed during update");
                }
            } else {
                cache.put(k, new ImmutableChoosableSetImpl<V>(v));
            }
            return null;
        }, completionHandler);
    }

    @Override
    public void remove(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
        vertx.executeBlocking(() -> {
            ImmutableChoosableSet<V> oldValue = cache.get(k);
            if (oldValue != null) {
                if (cache.replace(k, oldValue, oldValue.remove(v))) {
                    throw new RuntimeException("Value changed during update");
                }
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
