package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.callback.BooleanPredicateCallback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.FailureCallback;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSetImpl;
import org.vertx.java.spi.cluster.impl.infinispan.futures.FutureListenerHelper;
import org.vertx.java.spi.cluster.impl.infinispan.futures.NotifyingFutureHelper;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;

import java.util.Arrays;
import java.util.List;


public class InfinispanAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(InfinispanAsyncMultiMap.class);

    private final VertxSPI vertxSPI;
    private final Cache<K, ImmutableChoosableSet<V>> cache;

    public InfinispanAsyncMultiMap(VertxSPI vertxSPI, Cache<K, ImmutableChoosableSet<V>> cache) {
        this.vertxSPI = vertxSPI;
        this.cache = cache;
    }

    @Override
    public void get(final K k, final Handler<AsyncResult<ChoosableIterable<V>>> completionHandler) {
        NotifyingFutureHelper
                .perform(cache.getAsync(k))
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(new CompleteCallback<ImmutableChoosableSet<V>>(completionHandler));
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        NotifyingFutureHelper
                .perform(cache.getAsync(k))
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(
                        new Callback<ImmutableChoosableSet<V>>() {

                            @Override
                            public void execute(ImmutableChoosableSet<V> value) {
                                if(value==null) {
                                    NotifyingFutureHelper
                                            .perform(cache.putAsync(k, new ImmutableChoosableSetImpl<V>(v)))
                                            .onSuccess(new CompleteCallbackNoResult<ImmutableChoosableSet<V>>(completionHandler))
                                            .onError(new FailureCallback<>(completionHandler));
                                } else {
                                    NotifyingFutureHelper
                                            .perform(cache.replaceAsync(k, value, value.add(v)))
                                            .onSuccess(new BooleanPredicateCallback(completionHandler))
                                            .onError(new FailureCallback<>(completionHandler));
                                }
                            }
                        }
                );
    }

    @Override
    public void remove(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        NotifyingFutureHelper
                .perform(cache.getAsync(k))
                .onError(new FailureCallback<>(completionHandler))
                .onSuccess(new Callback<ImmutableChoosableSet<V>>() {

                    @Override
                    public void execute(ImmutableChoosableSet<V> value) {
                        if (value != null) {
                            NotifyingFutureHelper
                                    .perform(cache.replaceAsync(k, value, value.remove(v)))
                                    .onSuccess(new BooleanPredicateCallback(completionHandler))
                                    .onError(new FailureCallback<>(completionHandler));
                        }
                    }
                });
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("not yet implemented");
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
