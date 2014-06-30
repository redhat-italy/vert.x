package org.vertx.java.spi.cluster.impl.infinispan.domain;

import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.spi.cluster.impl.infinispan.domain.support.ElementNotFoundInSetException;

public interface ImmutableChoosableSet<T> extends ChoosableIterable<T> {

    ImmutableChoosableSet<T> add(T value);

    ImmutableChoosableSet<T> remove(T value) throws ElementNotFoundInSetException;

    T head();

    ImmutableChoosableSet<T> tail();
}
