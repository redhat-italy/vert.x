package org.vertx.java.spi.cluster.impl.infinispan;

import junit.framework.TestCase;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class TestMultipleCacheManager extends TestCase {

    private EmbeddedCacheManager cacheManager;

    public void testMultipleCacheManagerOne() {
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .clusteredDefault()
                .transport().addProperty("configurationFile", "jgroups-udp.xml")
                .globalJmxStatistics().allowDuplicateDomains(true).enable()
                .build();
        Configuration asyncConfiguration = new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.DIST_ASYNC)
                .hash().numOwners(2)
                .build();

        cacheManager = new DefaultCacheManager(globalConfiguration, asyncConfiguration);
        cacheManager.start();

        Cache<Object, Object> map = cacheManager.getCache("test", true);

        map.put("test", "value");

        cacheManager.stop();
    }

    public void testMultipleCacheManagerSecond() {
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .clusteredDefault()
                .transport().addProperty("configurationFile", "jgroups-udp.xml")
                .globalJmxStatistics().allowDuplicateDomains(true).enable()
                .build();
        Configuration asyncConfiguration = new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.DIST_ASYNC)
                .hash().numOwners(2)
                .build();

        cacheManager = new DefaultCacheManager(globalConfiguration, asyncConfiguration);
        cacheManager.start();

        Cache<Object, Object> map = cacheManager.getCache("test", true);

        map.put("test", "value");

        cacheManager.stop();
    }
}
