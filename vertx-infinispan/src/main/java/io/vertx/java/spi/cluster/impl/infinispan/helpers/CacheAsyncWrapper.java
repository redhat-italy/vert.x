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

package io.vertx.java.spi.cluster.impl.infinispan.helpers;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;

import java.util.function.Consumer;

public class CacheAsyncWrapper<K1, V1> {

  private final static Logger log = LoggerFactory.getLogger(CacheAsyncWrapper.class);

  private final Cache<K1, V1> cache;

  public CacheAsyncWrapper(Cache<K1, V1> cache) {
    this.cache = cache;
  }

  public void clear(Consumer<Void> onSuccess, Consumer<Throwable> onError) {
    cache.clearAsync().attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void get(K1 k, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<V1> async = cache.getAsync(k);
    log.debug(String.format("GET: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void put(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<V1> async = cache.putAsync(k, v);
    log.debug(String.format("PUT: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void putIfAbsent(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<V1> async = cache.putIfAbsentAsync(k, v);
    log.debug(String.format("PUTIFABSENT: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void replace(K1 k, V1 v, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<V1> async = cache.replaceAsync(k, v);
    log.debug(String.format("REPLACE: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void replace(K1 k, V1 oldValue, V1 newValue, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<Boolean> async = cache.replaceAsync(k, oldValue, newValue);
    log.debug(String.format("REPLACE Old-New: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void remove(K1 k, Consumer<V1> onSuccess, Consumer<Throwable> onError) {
    org.infinispan.util.concurrent.NotifyingFuture<V1> async = cache.removeAsync(k);
    log.debug(String.format("REMOVE: Attach listener to the NotifyingFuture[%s]", async.getClass().getName()));
    async.attachListener(new FutureListenerHelper<>(onSuccess, onError));
  }

  public void removeIfPresent(K1 k, Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
    cache.removeAsync(k).attachListener(new FutureListenerHelper<>((v) -> onSuccess.accept(v != null), onError));
  }
}
