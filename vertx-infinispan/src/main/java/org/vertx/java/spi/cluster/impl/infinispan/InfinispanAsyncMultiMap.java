package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.helpers.CacheAsyncWrapper;


public class InfinispanAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(InfinispanAsyncMultiMap.class);

    private final CacheAsyncWrapper<K, ImmutableChoosableSet<V>> wrapper;

    public InfinispanAsyncMultiMap(Cache<K, ImmutableChoosableSet<V>> cache) {
        this.wrapper = new CacheAsyncWrapper<>(cache);
    }

    @Override
    public void get(final K k, final Handler<AsyncResult<ChoosableIterable<V>>> completionHandler) {
        wrapper
                .get(k)
                .onError(completionHandler)
                .onSuccess(new CompleteCallback<ImmutableChoosableSet<V>>(completionHandler));
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        wrapper
                .get(k)
                .onError(completionHandler)
                .onSuccess(new Callback<ImmutableChoosableSet<V>>() {

                    @Override
                    public void execute(ImmutableChoosableSet<V> value) {
                        wrapper
                                .put(k, value.add(v))
                                .onError(completionHandler)
                                .onSuccess(new CompleteCallbackNoResult<ImmutableChoosableSet<V>>(completionHandler));
                    }
                });
    }

    @Override
    public void remove(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        wrapper
                .get(k)
                .onError(completionHandler)
                .onSuccess(new Callback<ImmutableChoosableSet<V>>() {
                    @Override
                    public void execute(ImmutableChoosableSet<V> value) {
                        wrapper
                                .remove(k, value.remove(v))
                                .onError(completionHandler)
                                .onSuccess(new CompleteCallbackNoResult<ImmutableChoosableSet<V>>(completionHandler));
                    }
                });
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class CompleteCallback<T extends ChoosableIterable<V>> implements Callback<T> {

        private Handler<AsyncResult<ChoosableIterable<V>>> handler;

        public CompleteCallback(Handler<AsyncResult<ChoosableIterable<V>>> handler) {
            this.handler = handler;
        }

        @Override
        public void execute(T value) {
            DefaultFutureResult<ChoosableIterable<V>> result = new DefaultFutureResult<ChoosableIterable<V>>();
            result.setResult(value);
            result.complete();
            handler.handle(result);
        }
    }

    private class CompleteCallbackNoResult<T> implements Callback<T> {

        private Handler<AsyncResult<Void>> handler;

        public CompleteCallbackNoResult(Handler<AsyncResult<Void>> handler) {
            this.handler = handler;
        }

        @Override
        public void execute(T value) {
            DefaultFutureResult<Void> result = new DefaultFutureResult<Void>();
            result.setResult(null);
            result.complete();
            handler.handle(result);
        }
    }

}
