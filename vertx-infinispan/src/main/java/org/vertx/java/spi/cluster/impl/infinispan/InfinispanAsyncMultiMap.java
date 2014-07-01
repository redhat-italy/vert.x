package org.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.FutureResultImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import org.infinispan.Cache;
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
        FutureResultImpl<ChoosableIterable<V>> result = new FutureResultImpl<>();

        wrapper
                .get(k)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    completionHandler.handle(result);
                })
                .onSuccess((value) -> {
                    result.setResult(value);
                    result.complete();
                    completionHandler.handle(result);
                });
    }

    @Override
    public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .get(k)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    completionHandler.handle(result);
                })
                .onSuccess((value) -> {
                    wrapper
                            .put(k, value.add(v))
                            .onError((e) -> {
                                result.setFailure(e);
                                result.failed();
                                completionHandler.handle(result);
                            })
                            .onSuccess((set) -> {
                                result.setResult(null);
                                result.complete();
                                completionHandler.handle(result);
                            });
                });
    }

    @Override
    public void remove(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
        FutureResultImpl<Void> result = new FutureResultImpl<>();

        wrapper
                .get(k)
                .onError((e) -> {
                    result.setFailure(e);
                    result.failed();
                    completionHandler.handle(result);
                })
                .onSuccess((value) -> {
                    wrapper
                            .remove(k, value.remove(v))
                            .onError((e) -> {
                                result.setFailure(e);
                                result.failed();
                                completionHandler.handle(result);
                            })
                            .onSuccess((set) -> {
                                result.setResult(null);
                                result.complete();
                                completionHandler.handle(result);
                            });
                });
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
