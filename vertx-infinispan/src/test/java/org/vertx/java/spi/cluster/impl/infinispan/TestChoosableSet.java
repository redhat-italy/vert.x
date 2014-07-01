package org.vertx.java.spi.cluster.impl.infinispan;

import org.junit.Assert;
import org.junit.Test;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSet;
import org.vertx.java.spi.cluster.impl.infinispan.domain.ImmutableChoosableSetImpl;

import java.util.Iterator;

public class TestChoosableSet {

    @Test
    public void testOneElementSet() {
        String expected = "expected";
        ImmutableChoosableSet<String> set = new ImmutableChoosableSetImpl<>(expected);

        Assert.assertEquals(expected, set.choose());
        Assert.assertEquals(expected, set.choose());
    }

    @Test
    public void testTwoElementSet() {
        String expectedOne = "expectedOne";
        String expectedTwo = "expectedTwo";
        ImmutableChoosableSet<String> set = new ImmutableChoosableSetImpl<>(expectedOne).add(expectedTwo);

        Assert.assertEquals(expectedTwo, set.choose());
        Assert.assertEquals(expectedOne, set.choose());
        Assert.assertEquals(expectedTwo, set.choose());
        Assert.assertEquals(expectedOne, set.choose());
    }

    @Test
    public void testMoreElementSet() {
        String expectedOne = "expectedOne";
        String expectedTwo = "expectedTwo";
        String expectedThree = "expectedThree";
        String expectedFour = "expectedFour";
        String expectedFive = "expectedFive";

        ImmutableChoosableSet<String> value = new ImmutableChoosableSetImpl<>(expectedOne).add(expectedTwo);

        Assert.assertEquals(expectedTwo, value.choose());
        Assert.assertEquals(expectedOne, value.choose());

        value = value.add(expectedThree).add(expectedFour).add(expectedFive);

        Assert.assertEquals(expectedFive, value.choose());
        Assert.assertEquals(expectedFour, value.choose());
        Assert.assertEquals(expectedThree, value.choose());
        Assert.assertEquals(expectedTwo, value.choose());
        Assert.assertEquals(expectedOne, value.choose());
        Assert.assertEquals(expectedFive, value.choose());
        Assert.assertEquals(expectedFour, value.choose());
        Assert.assertEquals(expectedThree, value.choose());
        Assert.assertEquals(expectedTwo, value.choose());
        Assert.assertEquals(expectedOne, value.choose());
    }

    @Test
    public void testIteratorOneElementSet() {
        String expected = "expected";

        ImmutableChoosableSet<String> value = new ImmutableChoosableSetImpl<>(expected);

        Iterator<String> iterator = value.iterator();
        Assert.assertEquals(expected, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteratorTwoElementSet() {
        String expectedOne = "expectedOne";
        String expectedTwo = "expectedTwo";

        ImmutableChoosableSet<String> value = new ImmutableChoosableSetImpl<>(expectedOne).add(expectedTwo);

        Iterator<String> iterator = value.iterator();
        Assert.assertEquals(expectedTwo, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedOne, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteratorMoreElementSet() {
        String expectedOne = "expectedOne";
        String expectedTwo = "expectedTwo";
        String expectedThree = "expectedThree";
        String expectedFour = "expectedFour";
        String expectedFive = "expectedFive";

        ImmutableChoosableSet<String> value = new ImmutableChoosableSetImpl<>(expectedOne).add(expectedTwo);

        Iterator<String> iterator = value.iterator();
        Assert.assertEquals(expectedTwo, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedOne, iterator.next());
        Assert.assertFalse(iterator.hasNext());

        value = value.add(expectedThree).add(expectedFour).add(expectedFive);

        iterator = value.iterator();
        Assert.assertEquals(expectedFive, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedFour, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedThree, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedTwo, iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(expectedOne, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }
}
