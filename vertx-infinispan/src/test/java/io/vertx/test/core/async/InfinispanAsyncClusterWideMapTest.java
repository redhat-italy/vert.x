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

import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.java.spi.cluster.impl.infinispan.async.InfinispanAsyncClusterManager;
import io.vertx.test.core.ClusterWideMapTestDifferentNodes;
import org.junit.Test;

public class InfinispanAsyncClusterWideMapTest extends ClusterWideMapTestDifferentNodes {

  public InfinispanAsyncClusterWideMapTest() {
    disableThreadChecks();
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new InfinispanAsyncClusterManager();
  }

  @Test
  public void testBoolean() {
    String k = "foo";
    Boolean v = true;
    Boolean other = false;

    getVertx().sharedData().<String, Boolean>getClusterWideMap("foo", ar -> {
      assertTrue(ar.succeeded());
      AsyncMap<String, Boolean> map = ar.result();
      map.put(k, v, ar2 -> {
        assertTrue(ar2.succeeded());
        assertNull(ar2.result());
        getVertx().sharedData().<String, Boolean>getClusterWideMap("foo", ar3 -> {
          AsyncMap<String, Boolean> map2 = ar.result();
          map2.replace(k, other, ar4 -> {
            assertEquals(v, ar4.result());
            map2.get(k, ar5 -> {
              assertEquals(other, ar5.result());
              map2.remove(k, ar6 -> {
                assertTrue(ar6.succeeded());
                map2.replace(k, other, ar7 -> {
                  assertNull(ar7.result());
                  map2.get(k, ar8 -> {
                    assertNull(ar8.result());
                    testComplete();
                  });
                });
              });
            });
          });
        });
      });
    });
    await();
  }

}
