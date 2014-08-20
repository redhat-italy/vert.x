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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.AsyncResultFailFast;
import org.infinispan.Cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PermitsManagerImpl implements PermitsManager {

    private final static String LOCK_COUNTER_KEY = "__vertx_lock_counter_key";
    private final static String LOCK_KEY_PREFIX = "__vertx_lock_key_prefix";

    private final static Logger log = LoggerFactory.getLogger(PermitsManagerImpl.class);

    private VertxSPI vertx;
    private CounterFactory counterFactory;

    private Cache<String, Long> lockCache;

    public PermitsManagerImpl(VertxSPI vertx, CounterFactory counterFactory, Cache<String, Long> cache) {
        this.vertx = vertx;
        this.counterFactory = counterFactory;
        this.lockCache = cache;
    }

    @Override
    public void acquireLock(long timeout, Handler<AsyncResult<Lock>> lock) {
        vertx.<Lock>executeBlocking(() -> {
                    CountDownLatch latch = new CountDownLatch(1);
                    counterFactory.getCounter(LOCK_COUNTER_KEY,
                            new AsyncResultFailFast<Counter>(
                                    (counter) -> counter.incrementAndGet(new AsyncResultFailFast<Long>(
                                            ()
                                    ))
                            )
                    );
                    try {
                        boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                    }
                }
        );
    }

    @Override
    public void releaseLock(Lock lock) {

    }
}