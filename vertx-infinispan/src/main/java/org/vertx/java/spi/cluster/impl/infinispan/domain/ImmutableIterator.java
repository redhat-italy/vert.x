package org.vertx.java.spi.cluster.impl.infinispan.domain;

import java.util.Collections;
import java.util.Iterator;

public class ImmutableIterator<T> implements Iterator<T> {

    private T value;
    private Iterator<T> nextIterator = Collections.emptyIterator();

    private boolean onNextIterator = false;

    public ImmutableIterator(T value) {
        this.value = value;
    }

    public ImmutableIterator(T value, Iterator<T> nextIterator) {
        this.value = value;
        this.nextIterator = nextIterator;
    }

    @Override
    public boolean hasNext() {
        if(!onNextIterator) {
            return true;
        }
        return nextIterator.hasNext();
    }

    @Override
    public T next() {
        if(!onNextIterator) {
            onNextIterator = true;
            return value;
        }
        return nextIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
