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

import io.vertx.test.core.VertxTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestInfinispanCounterImpl extends VertxTestBase {


    @Test
    public void testIncrementAndGet() {
        AtomicBoolean called = new AtomicBoolean(false);

        InfinispanCounterImpl counter = new InfinispanCounterImpl("counter", new DummyCache());

        counter.get((value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(0L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());

        called.set(false);
        counter.incrementAndGet((value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(1L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());

        called.set(false);
        counter.getAndIncrement((value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(1L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());

        called.set(false);
        counter.addAndGet(5, (value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(7L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());

        called.set(false);
        counter.getAndAdd(5, (value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(7L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());

        called.set(false);
        counter.get((value) -> {
            assertTrue(value.succeeded());
            assertNotNull(value.result());
            assertEquals(12L, value.result().longValue());

            called.set(true);
        });
        assertTrue(called.get());
    }

}
