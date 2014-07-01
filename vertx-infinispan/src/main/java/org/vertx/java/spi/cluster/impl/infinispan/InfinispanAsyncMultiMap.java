package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.compatibility.DefaultFutureResult;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.FailureCallback;
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
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(new CompleteCallback<ImmutableChoosableSet<V>>(completionHandler));
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        wrapper
                .get(k)
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(new Callback<ImmutableChoosableSet<V>>() {

                    @Override
                    public void execute(ImmutableChoosableSet<V> value) {
                        wrapper
                                .put(k, value.add(v))
                                .onError(new FailureCallback<>(completionHandler))
                                .onSuccess(new CompleteCallbackNoResult<ImmutableChoosableSet<V>>(completionHandler));
                    }
                });
    }

    @Override
    public void remove(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        wrapper
                .get(k)
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(new Callback<ImmutableChoosableSet<V>>() {
                    @Override
                    public void execute(ImmutableChoosableSet<V> value) {
                        wrapper
                                .remove(k, value.remove(v))
                                .onError(new FailureCallback<>(completionHandler))
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
