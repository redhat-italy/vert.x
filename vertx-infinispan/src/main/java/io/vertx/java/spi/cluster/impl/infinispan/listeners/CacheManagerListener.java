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

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;

import java.util.List;

@Listener(primaryOnly = true, sync = true)
public class CacheManagerListener {

    private final static Logger LOG = LoggerFactory.getLogger(CacheManagerListener.class);

    private NodeListener nodeListener;

    public CacheManagerListener(NodeListener nodeListener) {
        this.nodeListener = nodeListener;
    }

    @ViewChanged
    public void viewChangedEvent(ViewChangedEvent event) {
        List<Address> oldMembers = event.getOldMembers();
        List<Address> newMembers = event.getNewMembers();
        for (Address member : newMembers) {
            if (!oldMembers.contains(member)) {
                LOG.info(String.format("EVENT: ADDED MEMBER [%s]", member));
                nodeListener.nodeAdded(member.toString());
            }
        }
        for (Address member : oldMembers) {
            if (!newMembers.contains(member)) {
                LOG.info(String.format("EVENT: REMOVED MEMBER [%s]", member));
                nodeListener.nodeLeft(member.toString());
            }
        }
    }
}
