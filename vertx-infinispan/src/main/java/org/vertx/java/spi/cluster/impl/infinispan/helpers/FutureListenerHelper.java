package org.vertx.java.spi.cluster.impl.infinispan.helpers;

import org.infinispan.commons.util.concurrent.FutureListener;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.ExceptionCallback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.FailureCallback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.UndefinedCallback;

import java.util.concurrent.Future;

public class FutureListenerHelper<T> implements FutureListener<T> {

    private Callback<Exception> onError = UndefinedCallback.instance;
    private Callback<T> onSuccess = UndefinedCallback.instance;

    @Override
    public void futureDone(Future<T> future) {
        if(future.isDone()) {
            try {
                this.onSuccess.execute(future.get());
            } catch (Exception e) {
                this.onError.execute(e);
            }
        } else {
            this.onError.execute(null);
        }
    }

    public FutureListenerHelper<T> onError(ExceptionCallback onError) {
        this.onError = onError;
        return this;
    }

    public FutureListenerHelper<T> onSuccess(Callback<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }
}
