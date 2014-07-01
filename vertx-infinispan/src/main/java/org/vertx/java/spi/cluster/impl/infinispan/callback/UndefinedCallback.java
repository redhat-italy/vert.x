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

package org.vertx.java.spi.cluster.impl.infinispan.callback;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

public class UndefinedCallback implements Callback {

    private final static Logger LOG = LoggerFactory.getLogger(UndefinedCallback.class);

    public static Callback instance = new UndefinedCallback();

    @Override
    public void execute(Object value) {
        LOG.warn("Undefined callback");
    }
}
