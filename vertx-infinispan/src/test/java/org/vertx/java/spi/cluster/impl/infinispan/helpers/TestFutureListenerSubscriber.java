package org.vertx.java.spi.cluster.impl.infinispan.helpers;

import junit.framework.Assert;
import org.junit.Test;
import org.vertx.java.spi.cluster.impl.infinispan.callback.Callback;
import org.vertx.java.spi.cluster.impl.infinispan.callback.ExceptionCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFutureListenerSubscriber {

    @Test
    public void testSuccess() {
        final String expected = "expected";
        final AtomicInteger successCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>();
        subscriber.onSuccess(new Callback<String>() {
            @Override
            public void execute(String value) {
                successCounter.incrementAndGet();
                Assert.assertEquals(expected, value);
            }
        });
        subscriber.onError(new ExceptionCallback() {
            @Override
            public void execute(Exception value) {
                Assert.fail();
            }
        });

        subscriber.futureDone(new StringFutureMock(expected));

        Assert.assertEquals(1, successCounter.get());
    }

    @Test
    public void testException() {
        final InterruptedException expected = new InterruptedException("expected");
        final AtomicInteger errorCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>();
        subscriber.onSuccess(new Callback<String>() {
            @Override
            public void execute(String value) {
                Assert.fail();
            }
        });
        subscriber.onError(new ExceptionCallback() {
            @Override
            public void execute(Exception value) {
                errorCounter.incrementAndGet();

                Assert.assertEquals(expected, value);
            }
        });

        subscriber.futureDone(new ExceptionFutureMock(expected));
        Assert.assertEquals(1, errorCounter.get());
    }

    @Test
    public void testIsNotDone() {
        final AtomicInteger errorCounter = new AtomicInteger(0);

        FutureListenerHelper<String> subscriber = new FutureListenerHelper<>();
        subscriber.onSuccess(new Callback<String>() {
            @Override
            public void execute(String value) {
                Assert.fail();
            }
        });
        subscriber.onError(new ExceptionCallback() {
            @Override
            public void execute(Exception value) {
                errorCounter.incrementAndGet();
            }
        });

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
