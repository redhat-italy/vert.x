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

package org.vertx.java.spi.cluster.impl.infinispan.domain;

import io.vertx.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.domain.support.ElementNotFoundInSetException;

public interface ImmutableChoosableSet<T> extends ChoosableIterable<T> {

    ImmutableChoosableSet<T> add(T value);

    ImmutableChoosableSet<T> remove(T value);

    T head();

    ImmutableChoosableSet<T> tail();
}
