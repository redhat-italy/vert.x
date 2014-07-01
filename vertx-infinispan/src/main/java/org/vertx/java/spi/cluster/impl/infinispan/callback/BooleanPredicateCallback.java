package org.vertx.java.spi.cluster.impl.infinispan.callback;

import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import org.vertx.java.spi.cluster.impl.compatibility.DefaultFutureResult;

public class BooleanPredicateCallback implements Callback<Boolean> {

    private final Handler<AsyncResult<Void>> handler;

    public BooleanPredicateCallback(Handler<AsyncResult<Void>> handler) {
        this.handler = handler;
    }

    @Override
    public void execute(Boolean value) {
        DefaultFutureResult<Void> result = new DefaultFutureResult<>();
        if (value) {
            result.setResult(null);
            result.complete();
        } else {
            result.failed();
        }
        handler.handle(result);
    }
}
