package org.vertx.java.spi.cluster.impl.infinispan.futures;

import org.infinispan.commons.util.concurrent.NotifyingFuture;

public class NotifyingFutureHelper {

    public static <T> FutureListenerHelper<T> perform(NotifyingFuture<T> future) {
        FutureListenerHelper<T> subscriber = new FutureListenerHelper<>();
        future.attachListener(subscriber);
        return subscriber;
    }

}
