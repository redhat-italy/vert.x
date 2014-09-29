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
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class HandlerHelper<V> {

    private Handler<AsyncResult<V>> handler;

    public HandlerHelper(Handler<AsyncResult<V>> handler) {
        this.handler = handler;
    }

    public final void success(V value) {
        handler.handle(Future.completedFuture(value));
    }

    public final void error(Throwable e) {
        handler.handle(Future.completedFuture(e));
    }
}
