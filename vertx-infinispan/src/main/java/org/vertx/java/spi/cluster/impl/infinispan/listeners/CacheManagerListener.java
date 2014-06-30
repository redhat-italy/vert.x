package org.vertx.java.spi.cluster.impl.infinispan.listeners;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.cluster.NodeListener;

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
