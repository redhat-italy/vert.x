package org.vertx.java.spi.cluster.impl.infinispan.callback;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.vertx.java.spi.cluster.impl.compatibility.DefaultFutureResult;


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
