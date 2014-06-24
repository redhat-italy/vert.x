package org.vertx.java.spi.cluster.impl.infinispan.domain;

import java.util.Iterator;

public class ImmutableChoosableSetImpl<T> implements ImmutableChoosableSet<T> {

    private T value;
    private ImmutableChoosableSet<T> choosableIterable = EmptyImmutableChoosableIterable.emptySet;

    private transient ImmutableChoosableSet<T> next = this;

    public ImmutableChoosableSetImpl(T value) {
        this.value = value;
    }

    @Override
    public ImmutableChoosableSet<T> add(T t) {
        ImmutableChoosableSetImpl<T> value = new ImmutableChoosableSetImpl<>(t);
        value.choosableIterable = this;
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public T choose() {
        if(next == EmptyImmutableChoosableIterable.emptySet) {
            next = choosableIterable;
            return value;
        } else {
            T result = ((ImmutableChoosableSetImpl<T>) next).value;
            next = ((ImmutableChoosableSetImpl<T>)next).choosableIterable;
            return result;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(value, choosableIterable.iterator());
    }

}
