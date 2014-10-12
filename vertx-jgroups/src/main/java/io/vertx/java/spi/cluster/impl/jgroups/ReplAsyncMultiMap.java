package io.vertx.java.spi.cluster.impl.jgroups;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ImmutableChoosableSet;
import io.vertx.java.spi.cluster.impl.jgroups.domain.serializer.ImmutableChoosableSetSerializer;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;

import java.io.ObjectOutput;

import static io.vertx.java.spi.cluster.impl.jgroups.domain.serializer.ImmutableChoosableSetSerializer.fromByteArray;
import static io.vertx.java.spi.cluster.impl.jgroups.domain.serializer.ImmutableChoosableSetSerializer.toByteArray;

public class ReplAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

  private final ReplicatedHashMap<K, byte[]> map;
  private String name;
  private final VertxSPI vertx;

  public ReplAsyncMultiMap(String name, JChannel channel, VertxSPI vertx) throws Exception {
    this.name = name;
    this.vertx = vertx;
    this.map = new ReplicatedHashMap<>(channel);
    this.map.start(2000);
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
    vertx.sharedData().getLock(name + k, (lock) -> {
      if (lock.succeeded()) {
        try {
          ImmutableChoosableSet<V> entry = fromByteArray(map.get(k));
          if (entry == null) {
            entry = ImmutableChoosableSet.emptySet;
          }
          map.put(k, toByteArray(entry.add(v)));
          lock.result().release();
          handler.handle(Future.completedFuture(null));
        } catch (Exception e) {
          handler.handle(Future.completedFuture(e));
        }
      } else {
        handler.handle(Future.completedFuture(lock.cause()));
      }
    });
  }

  @Override
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    vertx.executeBlocking(() -> fromByteArray(map.get(k)), handler);
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    vertx.sharedData().getLock(name + k, (lock) -> {
      if (lock.succeeded()) {
        try {
          ImmutableChoosableSet<V> entry = fromByteArray(map.get(k));
          if (entry != null) {
            map.put(k, toByteArray(entry.remove(v)));
            handler.handle(Future.completedFuture(true));
          } else {
            handler.handle(Future.completedFuture(false));
          }
          lock.result().release();
        } catch (Exception e) {
          handler.handle(Future.completedFuture(e));
        }
      } else {
        handler.handle(Future.completedFuture(lock.cause()));
      }
    });
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
    throw new UnsupportedOperationException("not yet implemented.");
  }
}
