package org.vertx.java.spi.cluster.impl.infinispan.callback;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

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
