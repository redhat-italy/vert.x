package org.vertx.java.spi.cluster.impl.infinispan.callback;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class UndefinedCallback implements Callback {

    private final static Logger LOG = LoggerFactory.getLogger(UndefinedCallback.class);

    public static Callback instance = new UndefinedCallback();

    @Override
    public void execute(Object value) {
        LOG.warn("Undefined callback");
    }
}
