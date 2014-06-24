package org.vertx.java.spi.cluster.impl.infinispan.domain;

import java.util.Collections;
import java.util.Iterator;

public class EmptyImmutableChoosableIterable<T> implements ImmutableChoosableSet<T> {

    public static final ImmutableChoosableSet emptySet = new EmptyImmutableChoosableIterable();

    private EmptyImmutableChoosableIterable() {
    }

    @Override
    public ImmutableChoosableSet<T> add(T t) {
        return new ImmutableChoosableSetImpl<>(t);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public T choose() {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }
}
