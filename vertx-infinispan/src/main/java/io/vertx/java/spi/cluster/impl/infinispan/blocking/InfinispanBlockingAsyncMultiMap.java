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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LogDelegate;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSetImpl;
import org.infinispan.Cache;

public class InfinispanBlockingAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanBlockingAsyncMultiMap.class);

  private final Logger log = new Logger(new LogDelegate() {
    @Override
    public boolean isInfoEnabled() {
      return LOGGER.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
      return LOGGER.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
      return LOGGER.isTraceEnabled();
    }

    @Override
    public void fatal(Object message) {
      LOGGER.fatal(String.format("[%s] - %s", node, message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
      LOGGER.fatal(String.format("[%s] - %s", node, message), t);
    }

    @Override
    public void error(Object message) {
      LOGGER.error(String.format("[%s] - %s", node, message));
    }

    @Override
    public void error(Object message, Throwable t) {
      LOGGER.error(String.format("[%s] - %s", node, message), t);
    }

    @Override
    public void warn(Object message) {
      LOGGER.warn(String.format("[%s] - %s", node, message));
    }

    @Override
    public void warn(Object message, Throwable t) {
      LOGGER.warn(String.format("[%s] - %s", node, message), t);
    }

    @Override
    public void info(Object message) {
      LOGGER.info(String.format("[%s] - %s", node, message));
    }

    @Override
    public void info(Object message, Throwable t) {
      LOGGER.info(String.format("[%s] - %s", node, message), t);
    }

    @Override
    public void debug(Object message) {
      LOGGER.debug(String.format("[%s] - %s", node, message));
    }

    @Override
    public void debug(Object message, Throwable t) {
      LOGGER.debug(String.format("[%s] - %s", node, message), t);
    }

    @Override
    public void trace(Object message) {
      LOGGER.trace(String.format("[%s] - %s", node, message));
    }

    @Override
    public void trace(Object message, Throwable t) {
      LOGGER.trace(String.format("[%s] - %s", node, message), t);
    }
  });

  private String node;
  private final VertxSPI vertx;
  private final Cache<K, ImmutableChoosableSet<V>> cache;

  public InfinispanBlockingAsyncMultiMap(String node, VertxSPI vertx, Cache<K, ImmutableChoosableSet<V>> cache) {
    this.node = node;
    this.vertx = vertx;
    this.cache = cache;
  }

  @Override
  public void get(final K k, Handler<AsyncResult<ChoosableIterable<V>>> completionHandler) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("Getting value for key [%s]", k));
    }
    vertx.executeBlocking(() -> {
          ImmutableChoosableSet<V> v = cache.get(k);
          if (log.isDebugEnabled()) {
            log.debug(String.format("Value [%s] for key [%s]", v, k));
          }
          return v;
        }, completionHandler
    );
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
    vertx.executeBlocking(() -> {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Remove value [%s] for key [%s]", v, k));
      }
      try {
        ImmutableChoosableSet<V> oldValue = cache.get(k);
        if (log.isDebugEnabled()) {
          log.debug(String.format("Value from cache [%s]", oldValue == null ? "null" : oldValue));
        }

        return (oldValue == null || oldValue.isEmpty()) || cache.replace(k, oldValue, oldValue.remove(v));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }, completionHandler);
  }

  @Override
  public void add(final K k, final V v, final Handler<AsyncResult<Void>> completionHandler) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("Add value [%s] to key [%s]", v, k));
    }
    vertx.executeBlocking(() -> {
      ImmutableChoosableSetImpl<V> newValue = new ImmutableChoosableSetImpl<>(v);
      ImmutableChoosableSet<V> entry = cache.putIfAbsent(k, newValue);
      if (log.isDebugEnabled()) {
        log.debug(String.format("PutIfAbsent in map [%s, %s] = %s", k, newValue, entry));
      }
      if (entry != null) {
        if (log.isDebugEnabled()) {
          log.debug(String.format("Replace key [%s] value in map %s with %s", k, entry, entry.add(v)));
        }
        if (!cache.replace(k, entry, entry.add(v))) {
          log.error(String.format("Value changed during update for key [%s], retry []", k.toString()));
          throw new RuntimeException("Value changed during update");
        }
      }
      return null;
    }, completionHandler);
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
    log.error("not yet implemented");
    //This should be a distributed execute on primary(local) key
    throw new UnsupportedOperationException("not yet implemented");
  }
}
