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

package io.vertx.java.spi.cluster.impl.infinispan.async;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.HandlerHelper;
import org.infinispan.Cache;
import io.vertx.java.spi.cluster.impl.infinispan.helpers.CacheAsyncWrapper;


public class InfinispanAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

  private static final Logger log = LoggerFactory.getLogger(InfinispanAsyncMultiMap.class);

  private final CacheAsyncWrapper<K, ImmutableChoosableSet<V>> wrapper;

  public InfinispanAsyncMultiMap(Cache<K, ImmutableChoosableSet<V>> cache) {
    this.wrapper = new CacheAsyncWrapper<>(cache);
  }

  @Override
  public void get(final K k, final Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    HandlerHelper<ChoosableIterable<V>> helper = new HandlerHelper<>(handler);

    wrapper
        .get(k,
            helper::success,
            helper::error
        );
  }

  @Override
  public void add(final K k, final V v, final Handler<AsyncResult<Void>> handler) {
    HandlerHelper<Void> helper = new HandlerHelper<>(handler);

    wrapper
        .get(k,
            (value) -> {
              if (value != null) {
                wrapper
                    .put(k, value.add(v),
                        (newValue) -> helper.success(null),
                        helper::error
                    );
              } else {
                helper.success(null);
              }
            },
            helper::error
        );
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    HandlerHelper<Boolean> helper = new HandlerHelper<>(handler);

    wrapper
        .get(k,
            (value) -> {
              if (value != null) {
                if (value.isEmpty()) {
                  wrapper.removeIfPresent(k,
                      helper::success,
                      helper::error
                  );
                } else {
                  wrapper.replace(k, value, value.remove(v),
                      helper::success,
                      helper::error
                  );
                }
              } else {
                helper.success(false);
              }
            },
            helper::error
        );
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
