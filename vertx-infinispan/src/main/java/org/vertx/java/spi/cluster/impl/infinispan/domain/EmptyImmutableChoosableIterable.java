package org.vertx.java.spi.cluster.impl.infinispan.domain;

import org.vertx.java.spi.cluster.impl.infinispan.domain.support.ElementNotFoundInSetException;

import java.util.Collections;
import java.util.Iterator;

public class EmptyImmutableChoosableIterable<T> implements ImmutableChoosableSet<T> {

    public static final ImmutableChoosableSet emptySet = new EmptyImmutableChoosableIterable();

    private EmptyImmutableChoosableIterable() {
    }

    @Override
    public ImmutableChoosableSet<T> add(T value) {
        return new ImmutableChoosableSetImpl<T>(value);
    }

    @Override
    public ImmutableChoosableSet<T> remove(T value) throws ElementNotFoundInSetException {
        throw new ElementNotFoundInSetException();
    }

    @Override
    public T head() {
        return null;
    }

    @Override
    public ImmutableChoosableSet<T> tail() {
        return this;
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
