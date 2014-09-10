/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.java.spi.cluster.impl.infinispan.helpers;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.infinispan.commons.util.concurrent.FutureListener;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FutureListenerHelper<T> implements FutureListener<T>, Handler<AsyncResult<T>> {

    private Consumer<Throwable> onError;
    private Stream<Consumer<T>> onSuccess;

    public FutureListenerHelper(Consumer<T> onSuccess, Consumer<Throwable> onError) {
        this.onSuccess = Stream.of(onSuccess);
        this.onError = onError;
    }

    private void done(T value) {
        this.onSuccess.forEachOrdered((c) -> c.accept(value));
    }

    private void error(Throwable value) {
        this.onError.accept(value);
    }

    @Override
    public void futureDone(Future<T> future) {
        if (future.isDone()) {
            try {
                done(future.get());
            } catch (InterruptedException | ExecutionException e) {
                error(e);
            }
        } else {
            error(null);
        }
    }

    @Override
    public void handle(AsyncResult<T> future) {
        if (future.succeeded()) {
            done(future.result());
        } else {
            error(future.cause());
        }
    }

}
