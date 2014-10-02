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

package io.vertx.java.spi.cluster.impl.infinispan.blocking;

import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.java.spi.cluster.impl.infinispan.InfinispanClusterManagerBase;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.infinispan.Cache;

public class InfinispanBlockingClusterManager extends InfinispanClusterManagerBase {

  @Override
  protected final <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(Cache<K, ImmutableChoosableSet<V>> map) {
    return new InfinispanBlockingAsyncMultiMap<>(this.getNodeID(), this.getVertx(), map);
  }

  @Override
  protected final <K, V> AsyncMap<K, V> getAsyncMap(Cache<K, V> map) {
    return new InfinispanBlockingAsyncMap<>(this.getNodeID(), this.getVertx(), map);
  }

}
