package io.vertx.java.spi.cluster.impl.jgroups;

import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.VertxSPI;
import io.vertx.java.spi.cluster.impl.jgroups.domain.AsyncMultiMapWrapper;
import io.vertx.java.spi.cluster.impl.jgroups.domain.MultiMapImpl;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ReplicatedMultiMapManager {

  public static final RequestOptions REQUEST_OPTIONS_BLOCKING = new RequestOptions().setMode(ResponseMode.GET_ALL);

  private static final Logger log = LoggerFactory.getLogger(ReplicatedMultiMapManager.class);

  private final VertxSPI vertx;
  private final JChannel channel;
  private final RpcDispatcher dispatcher;
  private final Map<String, MultiMapImpl> multiMaps = new HashMap<>();

  private final static Map<Short, Method> methods = new HashMap<Short, Method>();

  private static final short CREATE = 0;
  public static final short ADD = 1;
  public static final short REMOVE = 3;

  static {
    try {
      methods.put(CREATE, ReplicatedMultiMapManager.class.getMethod("_create", String.class));
      methods.put(ADD, ReplicatedMultiMapManager.class.getMethod("_add", String.class, Object.class, Object.class));
      methods.put(REMOVE, ReplicatedMultiMapManager.class.getMethod("_remove", String.class, Object.class, Object.class));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public ReplicatedMultiMapManager(VertxSPI vertx, JChannel channel) {
    this.vertx = vertx;
    this.channel = channel;
    this.dispatcher = new RpcDispatcher(this.channel, this);
    this.dispatcher.setMethodLookup((id) -> methods.get(id));
  }

  public <K, V> AsyncMultiMap<K, V> createAsyncMultiMap(String name) {
    if (log.isTraceEnabled()) {
      log.trace(String.format("method createAsyncMultiMap address[%s] name[%s]", channel.getAddressAsString(), name));
    }
    try {
      MethodCall action = new MethodCall(ReplicatedMultiMapManager.CREATE, name);
      RspList<Object> responses = dispatcher.callRemoteMethods(null, action, REQUEST_OPTIONS_BLOCKING);
      Stream<Rsp<Object>> stream = responses.values().stream();
      if (log.isTraceEnabled()) {
        stream = stream.peek((rsp) -> log.trace(String.format("method createAsyncMultiMap address[%s] name[%s] response[%s]", channel.getAddressAsString(), name, rsp)));
      }
      Optional<Rsp<Object>> rspException = stream
          .filter((rsp) -> rsp != null && rsp.hasException())
          .findFirst();
      if (rspException.isPresent()) {
        throw rspException.get().getException();
      } else {
        return new AsyncMultiMapWrapper<K, V>(name, multiMaps.get(name), vertx, dispatcher);
      }
    } catch (Throwable e) {
      throw new VertxException(e);
    }
  }

  public <K, V> boolean _create(String name) {
    if (log.isTraceEnabled()) {
      log.trace(String.format("method _create address[%s] name[%s]", channel.getAddressAsString(), name));
    }
    multiMaps.computeIfAbsent(name, (key) -> new MultiMapImpl<>());
    return true;
  }

  public void _add(String name, Object key, Object value) {
    if (log.isTraceEnabled()) {
      log.trace(String.format("method _add address[%s] name[%s] key[%s] value[%s]", channel.getAddressAsString(), name, key, value));
    }
    Optional.of(multiMaps.get(name)).get().add(key, value);
  }

  public boolean _remove(String name, Object key, Object value) {
    if (log.isTraceEnabled()) {
      log.trace(String.format("method _remove address[%s] name[%s] key[%s] value[%s]", channel.getAddressAsString(), name, key, value));
    }
    return Optional.of(multiMaps.get(name)).get().remove(key, value);
  }

}
