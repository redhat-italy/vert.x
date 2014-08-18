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

import org.infinispan.commons.util.concurrent.FutureListener;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class FutureListenerHelper<T> implements FutureListener<T> {

    private Optional<Consumer<Exception>> onError;
    private Optional<Consumer<T>> onSuccess;

    public FutureListenerHelper(Consumer<T> onSuccess, Consumer<Exception> onError) {
        this.onSuccess = Optional.of(onSuccess);
        this.onError = Optional.of(onError);
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

}
