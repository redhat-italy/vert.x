package org.vertx.java.spi.cluster.impl.infinispan;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import junit.framework.Assert;
import org.junit.Test;

public class TestMultiMap {

    @Test
    public void test() {
        HazelcastInstance instance = Hazelcast.getOrCreateHazelcastInstance(new Config("prova"));

        MultiMap<String, String> map = instance.getMultiMap("pippo");

        map.put("key", "value1");
        map.put("key", "value2");
        map.put("key", "value3");

        map.put("key1", "value1");

        map.put("key2", "value1");

        Assert.assertEquals(5, map.entrySet().size());

        map.remove("key", "value1");
        Assert.assertEquals(4, map.entrySet().size());

        Assert.assertEquals(2, map.get("key").size());
    }
}
