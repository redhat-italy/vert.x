package org.vertx.java.spi.cluster.impl.infinispan.callback;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class Utils {

    public static Callback<Boolean> predicate(Handler<AsyncResult<Void>> handler) {
        return new BooleanPredicateCallback(handler);
    }
}
