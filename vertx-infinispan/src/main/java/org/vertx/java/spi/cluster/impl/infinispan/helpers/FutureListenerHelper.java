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

package org.vertx.java.spi.cluster.impl.infinispan.helpers;

import org.infinispan.commons.util.concurrent.FutureListener;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
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

    public FutureListenerHelper<T> onError(Callback<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public FutureListenerHelper<T> onSuccess(Callback<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }
}
