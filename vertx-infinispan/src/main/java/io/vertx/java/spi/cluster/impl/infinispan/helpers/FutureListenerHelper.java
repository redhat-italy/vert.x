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
import org.infinispan.commons.util.concurrent.BaseNotifyingFuture;
import org.infinispan.commons.util.concurrent.FutureListener;
import org.infinispan.commons.util.concurrent.NotifyingFutureAdaptor;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FutureListenerHelper<T> implements FutureListener<T> {

  private final static Logger log = LoggerFactory.getLogger(FutureListenerHelper.class);

  private final Consumer<T> onSuccess;
  private final Consumer<Throwable> onError;

  public FutureListenerHelper(Consumer<T> onSuccess, Consumer<Throwable> onError) {
    this.onSuccess = onSuccess;
    this.onError = onError;
  }

  @Override
  public void futureDone(Future<T> future) {
    try {
      logDebug(() -> String.format("Retrieving value from future[%s] isDone[%s]", future.getClass().getName(), future.isDone()));
      if (future.isCancelled()) {
        logDebug(() -> String.format("FutureDone is called but future is not Done isCancelled=[%s]", future.isCancelled()));
        this.onError.accept(null);
      } else {
        T t = future.get();
        logDebug(() -> String.format("Future value is [%s]", t));
        this.onSuccess.accept(t);
      }
    } catch (InterruptedException | ExecutionException e) {
      logDebug(() -> "Retrieving value raise an error.", e);
      this.onError.accept(e);
    }
  }

  private void logDebug(Supplier<String> supplier) {
    if (log.isDebugEnabled()) {
      log.debug(supplier.get());
    }
  }

  private void logDebug(Supplier<String> supplier, Throwable e) {
    if (log.isDebugEnabled()) {
      log.debug(supplier.get(), e);
    }
  }

}
