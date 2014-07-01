package org.vertx.java.spi.cluster.impl.infinispan.callback;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

public class Utils {

    public static Callback<Boolean> predicate(Handler<AsyncResult<Void>> handler) {
        return new BooleanPredicateCallback(handler);
    }
}
