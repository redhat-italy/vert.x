/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.java.spi.cluster.impl.jgroups.domain.serializer;

import io.vertx.java.spi.cluster.impl.jgroups.domain.ImmutableChoosableSet;

import java.io.*;

public class ImmutableChoosableSetSerializer {

  public static <V> void writeObject(ObjectOutput output, ImmutableChoosableSet<V> object) throws IOException {
    if (object!=null && !object.isEmpty()) {
      output.writeObject(object.head());
      writeObject(output, object.tail());
    } else {
      output.writeObject(null);
    }
  }

  public static <V> ImmutableChoosableSet<V> readObject(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    V o = (V) objectInput.readObject();
    if (o == null) {
      return ImmutableChoosableSet.emptySet;
    }
    return ImmutableChoosableSetSerializer.<V>readObject(objectInput).add(o);
  }

  public static <V> byte[] toByteArray(ImmutableChoosableSet<V> object) {
    try (
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
        ObjectOutputStream stream = new ObjectOutputStream(buffer)) {
      writeObject(stream, object);
      stream.flush();
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <V> ImmutableChoosableSet<V> fromByteArray(byte[] object) {
    try (
        ByteArrayInputStream buffer = new ByteArrayInputStream(object);
        ObjectInputStream stream = new ObjectInputStream(buffer)) {
      return readObject(stream);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
