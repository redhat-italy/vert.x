package io.vertx.java.spi.cluster.impl.jgroups.domain;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.ReplicatedMultiMapManager;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.util.Optional;
import java.util.stream.Stream;

public class AsyncMultiMapWrapper<K, V> implements AsyncMultiMap<K, V> {

  private final static Logger log = LoggerFactory.getLogger(AsyncMultiMapWrapper.class);

  private final String name;
  private final MultiMapImpl<K, V> map;
  private final VertxSPI vertx;
  private final RpcDispatcher dispatcher;
  private final RequestOptions requestOptions = new RequestOptions().setMode(ResponseMode.GET_ALL);

  public AsyncMultiMapWrapper(String name, MultiMapImpl<K, V> map, VertxSPI vertx, RpcDispatcher dispatcher) {
    this.name = name;
    this.map = map;
    this.vertx = vertx;
    this.dispatcher = dispatcher;
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
    if (log.isTraceEnabled()) {
      log.trace("add k = [" + k + "], v = [" + v + "], handler = [" + handler + "]");
    }
    vertx.executeBlocking(() -> {
      try {
        MethodCall action = new MethodCall(ReplicatedMultiMapManager.ADD, name, k, v);
        RspList<Object> responses = dispatcher.callRemoteMethods(null, action, requestOptions);
        Stream<Rsp<Object>> stream = responses.values().stream();
        if (log.isTraceEnabled()) {
          stream = stream.peek((rsp) -> log.trace(String.format("method add address[%s] name[%s] response[%s]", rsp.getSender(), name, rsp)));
        }
        Optional<Rsp<Object>> rspException = stream
            .filter((rsp) -> rsp != null && rsp.hasException())
            .findFirst();
        if (rspException.isPresent()) {
          throw rspException.get().getException();
        } else {
          return null;
        }
      } catch (Throwable e) {
        throw new VertxException(e);
      }
    }, handler);
  }

  @Override
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    if (log.isTraceEnabled()) {
      log.trace("get k = [" + k + "], handler = [" + handler + "]");
    }
    vertx.executeBlocking(() -> {
      ImmutableChoosableSet<V> result = map.get(k);
      log.debug("get k = [" + k + "], value = [" + result + "]");
      return result;
    }, handler);
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    if (log.isTraceEnabled()) {
      log.trace("remove k = [" + k + "], v = [" + v + "], handler = [" + handler + "]");
    }
    vertx.executeBlocking(() -> {
      try {
        MethodCall action = new MethodCall(ReplicatedMultiMapManager.REMOVE, name, k, v);
        RspList<Object> responses = dispatcher.callRemoteMethods(null, action, requestOptions);
        Stream<Rsp<Object>> stream = responses.values().stream();
        if (log.isTraceEnabled()) {
          stream = stream.peek((rsp) -> log.trace(String.format("method remove address[%s] name[%s] response[%s]", rsp.getSender(), name, rsp)));
        }
        Optional<Rsp<Object>> rspException = stream
            .filter((rsp) -> rsp != null && rsp.hasException())
            .findFirst();
        return (!rspException.isPresent());
      } catch (Exception e) {
        throw new VertxException(e);
      }
    }, handler);
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
    throw new VertxException(new UnsupportedOperationException("Not yet implemented."));
  }

}
