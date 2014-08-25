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
import io.vertx.java.spi.cluster.impl.infinispan.support.AsyncResultFailFastException;

import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.function.Consumer;

public class AsyncResultHelper<T> implements Handler<AsyncResult<T>> {

    private Optional<Consumer<T>> success = Optional.empty();
    private Optional<Consumer<Throwable>> error = Optional.empty();

    public static <T> Handler<AsyncResult<T>> failFast(Consumer<T> success) {
        return new AsyncResultHelper<T>().onSuccess(success).onError((e) -> {
            throw new AsyncResultFailFastException(null, e);
        });
    }

    @Override
    public void handle(AsyncResult<T> event) {
        if (event.succeeded()) {
            success.ifPresent((consumer) -> consumer.accept(event.result()));
        } else {
            error.ifPresent((consumer) -> consumer.accept(event.cause()));
        }
    }

    public AsyncResultHelper<T> onSuccess(Consumer<T> success) {
        this.success = Optional.of(success);
        return this;
    }

    public AsyncResultHelper<T> onError(Consumer<Throwable> error) {
        this.error = Optional.of(error);
        return this;
    }
}