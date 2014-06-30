package org.vertx.java.spi.cluster.impl.infinispan.domain;

import org.vertx.java.spi.cluster.impl.infinispan.domain.support.ElementNotFoundInSetException;

import java.util.Iterator;

public class ImmutableChoosableSetImpl<T> implements ImmutableChoosableSet<T> {

    private T value;
    private ImmutableChoosableSet<T> next;

    private transient ImmutableChoosableSet<T> roundRobinState = this;

    private ImmutableChoosableSetImpl(T value, ImmutableChoosableSet<T> next) {
        this.value = value;
        this.next = next;
    }

    public ImmutableChoosableSetImpl(T value) {
        this(value, EmptyImmutableChoosableIterable.emptySet);
    }

    @Override
    public ImmutableChoosableSet<T> add(T value) {
        checkSanity(value);

        return new ImmutableChoosableSetImpl<T>(value, this);
    }

    @Override
    public ImmutableChoosableSet<T> remove(T value) throws ElementNotFoundInSetException {
        checkSanity(value);

        if (value.equals(this.value)) {
            return next;
        }
        return next.remove(value).add(value);
    }

    @Override
    public T head() {
        return value;
    }

    @Override
    public ImmutableChoosableSet<T> tail() {
        return next;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public T choose() {
        if(this.roundRobinState.isEmpty()) {
            this.roundRobinState = this;
        }
        T value = (T) this.roundRobinState.head();
        this.roundRobinState = this.roundRobinState.tail();
        return value;
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(value, next.iterator());
    }

    private void checkSanity(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Not supported null value.");
        }
    }

}
