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

package io.vertx.java.spi.cluster.impl.infinispan.domain;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.HandlerHelper;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class InfinispanCounterImpl implements Counter {

    public final static String COUNTER_CACHE_NAME = "__counter__vertx";

    private String name;
    private AsyncMap<String, Long> cache;

    private UnaryOperator<Long> next = (a) -> a + 1;

    public InfinispanCounterImpl(String name, AsyncMap<String, Long> cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public void get(Handler<AsyncResult<Long>> handler) {
        cache.get(name, handler);
    }

    private void getAndAdd(UnaryOperator<Long> operator, BinaryOperator<Long> choose, Handler<AsyncResult<Long>> handler) {
        HandlerHelper<Long> helper = new HandlerHelper<>(handler);

        cache.get(name, (value) -> {
            if (value.succeeded()) {
                Long oldValue = value.result();
                Long newValue = operator.apply(oldValue);

                cache.replaceIfPresent(name, oldValue, newValue, (replace) -> {
                    if (replace.succeeded()) {
                        helper.success(choose.apply(oldValue, newValue));
                    } else {
                        helper.error(replace.cause());
                    }
                });
            } else {
                helper.error(value.cause());
            }
        });
    }

    @Override
    public void incrementAndGet(Handler<AsyncResult<Long>> handler) {
        getAndAdd((l) -> l + 1, (o, n) -> n, handler);
    }

    @Override
    public void getAndIncrement(Handler<AsyncResult<Long>> handler) {
        getAndAdd((l) -> l + 1, (o, n) -> o, handler);
    }

    @Override
    public void decrementAndGet(Handler<AsyncResult<Long>> handler) {
        getAndAdd((l) -> l - 1, (o, n) -> n, handler);
    }

    @Override
    public void addAndGet(long value, Handler<AsyncResult<Long>> handler) {
        getAndAdd((l) -> l + value, (o, n) -> n, handler);
    }

    @Override
    public void getAndAdd(long value, Handler<AsyncResult<Long>> handler) {
        getAndAdd((l) -> l + value, (o, n) -> o, handler);
    }

    @Override
    public void compareAndSet(long expected, long update, Handler<AsyncResult<Boolean>> handler) {
        HandlerHelper<Boolean> helper = new HandlerHelper<>(handler);

        cache.get(name, (value) -> {
            if (value.succeeded()) {
                if(expected == value.result()) {
                    cache.replaceIfPresent(name, expected, update, (replace) -> {
                        if (replace.succeeded()) {
                            helper.success(true);
                        } else {
                            helper.error(replace.cause());
                        }
                    });
                } else {
                    helper.success(false);
                }
            } else {
                helper.error(value.cause());
            }
        });
    }
}
