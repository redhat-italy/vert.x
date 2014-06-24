package org.vertx.java.spi.cluster.impl.infinispan;

import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.ClusterManagerFactory;

public class InfinispanClusterManagerFactory implements ClusterManagerFactory {

    @Override
    public ClusterManager createClusterManager(VertxSPI vertx) {
        return new InfinispanClusterManager(vertx);
    }
}
