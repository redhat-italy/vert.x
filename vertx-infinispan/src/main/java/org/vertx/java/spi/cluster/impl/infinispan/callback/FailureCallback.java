package org.vertx.java.spi.cluster.impl.infinispan.callback;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

public class FailureCallback<T> implements ExceptionCallback {

    private final Handler<AsyncResult<T>> handler;

    public FailureCallback(Handler<AsyncResult<T>> handler) {
        this.handler = handler;
    }

    @Override
    public void execute(Exception e) {
        DefaultFutureResult<T> result = new DefaultFutureResult<T>();
        result.setFailure(e);
        result.failed();
        handler.handle(result);
    }
}
