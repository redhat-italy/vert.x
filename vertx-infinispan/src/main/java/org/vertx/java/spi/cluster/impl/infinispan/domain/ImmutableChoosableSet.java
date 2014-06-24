package org.vertx.java.spi.cluster.impl.infinispan.domain;

import org.vertx.java.core.spi.cluster.ChoosableIterable;

public interface ImmutableChoosableSet<T> extends ChoosableIterable<T> {

    ImmutableChoosableSet<T> add(T t);
}
