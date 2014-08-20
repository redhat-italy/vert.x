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

import java.util.function.Consumer;

public class AsyncResultFailFast<T> implements Handler<AsyncResult<T>> {

    private String message;
    private AsyncResultFunctional<T> eventHandler;
    private Consumer<Throwable> error = (e) -> {
        throw new AsyncResultFailFastException(message, e);
    };

    public AsyncResultFailFast() {
        this((v) -> {}, null);
    }

    public AsyncResultFailFast(Consumer<T> success) {
        this(success, null);
    }

    public AsyncResultFailFast(Consumer<T> success, String message) {
        this.message = message;
        this.eventHandler = new AsyncResultFunctional<T>().onSuccess(success).onError(error);
    }

    @Override
    public void handle(AsyncResult<T> event) {
        eventHandler.handle(event);
    }
}