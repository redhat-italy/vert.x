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

package io.vertx.java.spi.cluster.impl.logging;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LogDelegate;

public class PrefixLogDelegate implements LogDelegate {

  private final Logger delegate;
  private final String prefix;

  public PrefixLogDelegate(Logger delagate, String prefix) {
    this.delegate = delagate;
    this.prefix = prefix;
  }

  @Override
  public boolean isInfoEnabled() {
    return delegate.isInfoEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return delegate.isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return delegate.isTraceEnabled();
  }

  @Override
  public void fatal(Object message) {
    delegate.fatal(formatMessage(message));
  }

  @Override
  public void fatal(Object message, Throwable t) {
    delegate.fatal(formatMessage(message), t);
  }

  @Override
  public void error(Object message) {
    delegate.error(formatMessage(message));
  }

  @Override
  public void error(Object message, Throwable t) {
    delegate.error(formatMessage(message), t);
  }

  @Override
  public void warn(Object message) {
    delegate.warn(formatMessage(message));
  }

  @Override
  public void warn(Object message, Throwable t) {
    delegate.warn(formatMessage(message), t);
  }

  @Override
  public void info(Object message) {
    delegate.info(formatMessage(message));
  }

  @Override
  public void info(Object message, Throwable t) {
    delegate.info(formatMessage(message), t);
  }

  @Override
  public void debug(Object message) {
    delegate.debug(formatMessage(message));
  }

  @Override
  public void debug(Object message, Throwable t) {
    delegate.debug(formatMessage(message), t);
  }

  @Override
  public void trace(Object message) {
    delegate.trace(formatMessage(message));
  }

  @Override
  public void trace(Object message, Throwable t) {
    delegate.trace(formatMessage(message), t);
  }

  private String formatMessage(Object message) {
    return prefix + message;
  }
}
