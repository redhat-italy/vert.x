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

package io.vertx.test.core.async;

import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.java.spi.cluster.impl.infinispan.async.InfinispanAsyncClusterManager;
import io.vertx.test.core.ClusteredAsynchronousLockTest;
import org.junit.Before;
import org.junit.Test;

public class InfinispanAsyncClusteredAsynchronousLockTest extends ClusteredAsynchronousLockTest {

  public InfinispanAsyncClusteredAsynchronousLockTest() {
    disableThreadChecks();
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new InfinispanAsyncClusterManager();
  }

  @Test
  public void testLockService() {
    System.out.println("**********************************************");
    System.out.println("Start new TEST");
    getVertx().sharedData().getLock("foo", ar -> {
      assertTrue(ar.succeeded());
      long start = System.currentTimeMillis();
      Lock lock = ar.result();
      vertx.setTimer(1000, tid -> {
        lock.release();
      });
      vertx.setTimer(1000, tid -> {
        getVertx().sharedData().getLockWithTimeout("foo", 1000, ar2 -> {
          assertTrue(ar2.succeeded());
          // Should be delayed
          assertTrue(System.currentTimeMillis() - start >= 1000);
          testComplete();
        });
      });
    });
    await();
  }
}
