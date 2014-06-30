package org.vertx.java.spi.cluster.impl.infinispan.callback;

public interface Callback<T> {

    void execute(T value);
}
