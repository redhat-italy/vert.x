package org.vertx.java.spi.cluster.impl.infinispan;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMap;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.NodeListener;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.listeners.CacheListener;
import org.vertx.java.spi.cluster.impl.infinispan.listeners.CacheManagerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfinispanClusterManager implements ClusterManager {

    private final Configuration syncConfiguration;
    private final Configuration asyncConfiguration;

    private EmbeddedCacheManager cacheManager;
    private VertxSPI vertxSPI;
    private boolean active = false;
    private NodeListener listener;

    public InfinispanClusterManager(VertxSPI vertxSPI) {
        this.vertxSPI = vertxSPI;
        this.syncConfiguration = new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.DIST_SYNC)
                .hash().numOwners(2)
                .build();
        this.asyncConfiguration = new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.DIST_ASYNC)
                .hash().numOwners(2)
                .build();
    }

    @Override
    public synchronized <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String name) {
        Cache<K, ImmutableChoosableSet<V>> cache = cacheManager.<K, ImmutableChoosableSet<V>>getCache(name, true);
        return new InfinispanAsyncMultiMap<K, V>(vertxSPI, cache);
    }

    @Override
    public synchronized <K, V> AsyncMap<K, V> getAsyncMap(String name) {
        Cache<K, V> cache = cacheManager.<K, V>getCache(name, true);
        return new InfinispanAsyncMap<>(vertxSPI, cache);
    }

    @Override
    public synchronized <K, V> Map<K, V> getSyncMap(String name) {
        cacheManager.defineConfiguration(name, syncConfiguration);
        return cacheManager.<K, V>getCache(name, true);
    }

    @Override
    public String getNodeID() {
        return cacheManager.getAddress().toString();
    }

    @Override
    public List<String> getNodes() {
        ArrayList<String> nodes = new ArrayList<>();
        for (Address address : cacheManager.getMembers()) {
            nodes.add(address.toString());
        }
        return nodes;
    }

    @Override
    public void nodeListener(NodeListener listener) {
        this.listener = listener;
        this.cacheManager.addListener(new CacheManagerListener(listener));
    }

    @Override
    public synchronized void join() {
        System.out.println("JOIN [" + this + "]");
        if (active) {
            return;
        }
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .clusteredDefault()
                .classLoader(GlobalConfiguration.class.getClassLoader())
                .transport().addProperty("configurationFile", "jgroups-udp.xml")
                .globalJmxStatistics().allowDuplicateDomains(true).enable()
                .build();
        cacheManager = new DefaultCacheManager(globalConfiguration, asyncConfiguration);
        cacheManager.start();

        cacheManager.defineConfiguration("configuration", syncConfiguration);
        active = true;
    }

    @Override
    public synchronized void leave() {
        System.out.println("LEAVE [" + active + "] ");
        if (!active) {
            return;
        }
        cacheManager.stop();
        cacheManager = null;
        active = false;
    }
}
