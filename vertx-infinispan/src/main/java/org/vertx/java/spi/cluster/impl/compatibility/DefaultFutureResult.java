package org.vertx.java.spi.cluster.impl.compatibility;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.FutureResultImpl;

public class DefaultFutureResult<T> implements Future<T> {

    private final FutureResultImpl<T> future;

    public DefaultFutureResult() {
        this.future = new FutureResultImpl<T>();
    }

    @Override
    public boolean complete() {
        return future.complete();  
    }

    @Override
    public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
        return future.setHandler(handler);  
    }

    @Override
    public Future<T> setResult(T result) {
        return future.setResult(result);  
    }

    @Override
    public Future<T> setFailure(Throwable throwable) {
        return future.setFailure(throwable);  
    }

    @Override
    public T result() {
        return future.result();  
    }

    @Override
    public Throwable cause() {
        return future.cause();  
    }

    @Override
    public boolean succeeded() {
        return future.succeeded();  
    }

    @Override
    public boolean failed() {
        return future.failed();  
    }
}
