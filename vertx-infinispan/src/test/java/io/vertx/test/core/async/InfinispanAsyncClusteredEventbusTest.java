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

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.java.spi.cluster.impl.infinispan.async.InfinispanAsyncClusterManager;
import io.vertx.test.core.ClusteredEventBusTest;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

public class InfinispanAsyncClusteredEventbusTest extends ClusteredEventBusTest {

    public InfinispanAsyncClusteredEventbusTest() {
        disableThreadChecks();
    }

    @Override
    protected ClusterManager getClusterManager() {
        return new InfinispanAsyncClusterManager();
    }

}
