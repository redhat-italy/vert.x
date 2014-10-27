package io.vertx.java.spi.cluster.impl.jgroups;

import org.jgroups.blocks.MethodCall;

public interface MethodCallInterface {

  public interface ZeroParameters extends MethodCallInterface {
    MethodCall method(String name);
  }

  public interface OneParameter extends MethodCallInterface {
    MethodCall method(String name, Object p1);
  }

  public interface TwoParameter extends MethodCallInterface {
    MethodCall method(String name, Object p1, Object p2);
  }

  public interface ThreeParameter extends MethodCallInterface {
    MethodCall method(String name, Object p1, Object p2, Object p3);
  }
}
