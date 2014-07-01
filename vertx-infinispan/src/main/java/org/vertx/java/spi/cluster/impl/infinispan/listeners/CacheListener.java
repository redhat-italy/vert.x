package org.vertx.java.spi.cluster.impl.infinispan.listeners;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryLoaded;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryLoadedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;

import java.util.List;

@Listener(primaryOnly = true, sync = true)
public class CacheListener {

    @CacheEntryLoaded
    public void cacheEntryLoaded(CacheEntryLoadedEvent event) {
        if(event.isPre()) {
            System.out.println("@CacheEntryLoaded NEW VALUE [" + event.getKey() + "," + event.getValue() + "]");
            System.out.println("@CacheEntryLoaded CACHE NAME [" + event.getCache().getName() + "]");
        }
    }

    @CacheEntryCreated
    public void cacheEntryCreated(CacheEntryCreatedEvent event) {
        if(event.isPre()) {
            System.out.println("@CacheEntryLoaded NEW VALUE [" + event.getKey() + "," + event.getValue() + "]");
            System.out.println("@CacheEntryLoaded CACHE NAME [" + event.getCache().getName() + "]");
        }
    }
}
