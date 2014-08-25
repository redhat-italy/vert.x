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

package io.vertx.java.spi.cluster.impl.infinispan;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.Counter;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.FutureListenerHelper;

public class CounterFactoryFluent implements CounterFactory {

    private CounterFactory factory;

    public CounterFactoryFluent(CounterFactory factory) {
        this.factory = factory;
    }

    public FutureListenerHelper<Counter> getCounter(String name) {
        FutureListenerHelper<Counter> helper = new FutureListenerHelper<>();
        factory.getCounter(name, helper);
        return helper;
    }

    @Override
    public final void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
        factory.getCounter(name, handler);
    }
}
