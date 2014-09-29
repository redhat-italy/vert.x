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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.java.spi.cluster.impl.infinispan.support.FutureDoneException;
import org.infinispan.commons.util.concurrent.FutureListener;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Stream;

//public class FutureListenerHelper<T> implements FutureListener<T>, Handler<AsyncResult<T>> {
public class FutureListenerHelper<T> implements FutureListener<T> {

  private final static Logger log = LoggerFactory.getLogger(FutureListenerHelper.class);

  private Consumer<Throwable> onError;
  private Consumer<T> onSuccess;

  public FutureListenerHelper(Consumer<T> onSuccess, Consumer<Throwable> onError) {
    this.onSuccess = onSuccess;
    this.onError = onError;
  }

  private void done(T v) {
    this.onSuccess.accept(v);
  }

  private void error(Throwable value) {
    this.onError.accept(value);
  }

  @Override
  public void futureDone(Future<T> future) {
    if (future.isDone()) {
      try {
        log.debug("Retrieving value from future [" + future + "]");
        done(future.get());
      } catch (InterruptedException | ExecutionException e) {
        log.debug("Retrieving value error. ", e);
        error(e);
      }
    } else {
      log.debug("FutureDone is called but future is not Done isCancelled=[" + future.isCancelled() + "]");
      error(null);
    }
  }

/*
  @Override
  public void handle(AsyncResult<T> future) {
    if (future.succeeded()) {
      log.debug("SUCCESS - Handle async result [" + future + "]");
      done(future.result());
    } else {
      log.debug("ERROR - Handle async result [" + future + "]");
      error(future.cause());
    }
  }
*/
}
