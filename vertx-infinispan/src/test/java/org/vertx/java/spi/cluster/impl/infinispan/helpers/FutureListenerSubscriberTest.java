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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureListenerSubscriberTest {

    @Test
    public void testSuccess() {
        final String expected = "expected";
        final AtomicInteger successCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>(
                (value) -> {
                    successCounter.incrementAndGet();
                    Assert.assertEquals(expected, value);
                },
                (e) -> Assert.fail());

        subscriber.futureDone(new StringFutureMock(expected));

        Assert.assertEquals(1, successCounter.get());
    }

    @Test
    public void testException() {
        final InterruptedException expected = new InterruptedException("expected");
        final AtomicInteger errorCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>(
                (value) -> Assert.fail(),
                (e) -> {
                    errorCounter.incrementAndGet();

                    Assert.assertEquals(expected, e);
                });

        subscriber.futureDone(new ExceptionFutureMock(expected));
        Assert.assertEquals(1, errorCounter.get());
    }

    @Test
    public void testIsNotDone() {
        final AtomicInteger errorCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>(
                (value) -> Assert.fail(),
                (e) -> errorCounter.incrementAndGet());

        subscriber.futureDone(new IsNotDoneFutureMock("value"));
        Assert.assertEquals(1, errorCounter.get());
    }

    private class StringFutureMock extends AbstractFutureMock<String> {

        public StringFutureMock(String value) {
            super(value);
        }
    }

    private class ExceptionFutureMock extends AbstractFutureMock<String> {

        private InterruptedException exception;

        public ExceptionFutureMock(InterruptedException exception) {
            super("");
            this.exception = exception;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            throw exception;
        }

        @Override
        public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw exception;
        }
    }

    private class IsNotDoneFutureMock extends AbstractFutureMock<String> {

        public IsNotDoneFutureMock(String value) {
            super(value);
        }

        @Override
        public boolean isDone() {
            return false;
        }
    }

    private abstract class AbstractFutureMock<T> implements Future<T> {

        private final T value;

        public AbstractFutureMock(T value) {
            this.value = value;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
