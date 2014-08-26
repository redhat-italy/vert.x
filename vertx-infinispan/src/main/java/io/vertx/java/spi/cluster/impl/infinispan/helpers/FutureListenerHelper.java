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

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class FutureListenerHelper<T> implements FutureListener<T>, Handler<AsyncResult<T>> {

    private Optional<Consumer<Throwable>> onError = Optional.empty();
    private Optional<Consumer<T>> onSuccess = Optional.empty();

    public FutureListenerHelper() {
    }

    public FutureListenerHelper(Consumer<T> onSuccess, Consumer<Throwable> onError) {
        this.onSuccess = Optional.of(onSuccess);
        this.onError = Optional.of(onError);
    }

    public FutureListenerHelper<T> then(Consumer<T> success) {
        this.onSuccess = Optional.of(success);
        return this;
    }

    public <R> FutureListenerHelper<R> andThen(Consumer<R> success) {
        FutureListenerHelper<T> self = this;
        FutureListenerHelper<R> composer = new FutureListenerHelper<>();
        composer.onSuccess = Optional.of((t) -> {
            self.onSuccess.ifPresent((c) -> c.accept(t));
        });
        return composer;
    }

    @Override
    public void futureDone(Future<T> future) {
        if (future.isDone()) {
            this.onSuccess.ifPresent((consumer) -> {
                try {
                    consumer.accept(future.get());
                } catch (InterruptedException | ExecutionException cause) {
                    this.onError.ifPresent((error) -> error.accept(cause));
                }
            });
        } else {
            this.onError.ifPresent((consumer) -> consumer.accept(null));
        }
    }

    @Override
    public void handle(AsyncResult<T> future) {
        if (future.succeeded()) {
            this.onSuccess.ifPresent((consumer) -> consumer.accept(future.result()));
        } else {
            this.onError.ifPresent((consumer) -> consumer.accept(future.cause()));
        }
    }
}
