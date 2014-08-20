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

package io.vertx.java.spi.cluster.impl.infinispan.listeners;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

@Listener(primaryOnly = true, sync = true)
public class PermitsCacheListener {

    public static final String VERTX_PERMITS_KEY = "__vertx__permits";
    private AdvancedCache<String, Long> cache;

    public PermitsCacheListener(AdvancedCache<String, Long> cache) {
        this.cache = cache;
    }

    @CacheEntryRemoved
    public void entryRemoved(CacheEntryRemovedEvent<String, Long> event) {
        Cache<String, Long> cache = event.getCache();

        Long permits = cache.get(VERTX_PERMITS_KEY);
        if (permits <= 0) {
            throw new IllegalArgumentException(String.format("Permits cannot be zero [%d]", permits));
        }

        if (cache.replace(VERTX_PERMITS_KEY, permits, permits - 1)) {

        }
    }
}
