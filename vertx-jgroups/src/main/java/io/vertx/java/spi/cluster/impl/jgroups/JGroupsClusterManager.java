package io.vertx.java.spi.cluster.impl.jgroups;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.MapOptions;
import io.vertx.core.spi.cluster.*;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ClusteredLockImpl;
import io.vertx.java.spi.cluster.impl.jgroups.domain.ClusteredCounterImpl;
import io.vertx.java.spi.cluster.impl.jgroups.listeners.TopologyListener;
import io.vertx.java.spi.cluster.impl.jgroups.protocols.VERTX_LOCK;
import org.jgroups.JChannel;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.COUNTER;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.pbcast.STATE;
import org.jgroups.stack.ProtocolStack;

import java.util.List;
import java.util.Map;

public class JGroupsClusterManager implements ClusterManager {

  private static final Logger log = LoggerFactory.getLogger(JGroupsClusterManager.class);
  public static final String VERTX_COUNTER_CHANNEL = "__vertx__counter_channel";
  public static final String VERTX_LOCK_CHANNEL = "__vertx__lock_channel";

  public static final String CLUSTER_NAME = "JGROUPS-CLUSTER";
  private VertxSPI vertx;

  private JChannel channel;

  private ForkChannel counterChannel;
  private CounterService counterService;

  private ForkChannel lockChannel;
  private LockService lockService;

  private volatile boolean active;
  private String address;
  private TopologyListener topologyListener;

  @Override
  public void setVertx(VertxSPI vertx) {
    this.vertx = vertx;
  }

  @Override
  public <K, V> void getAsyncMultiMap(String name, MapOptions options, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
    vertx.executeBlocking(() -> {
      try {
        return new ReplAsyncMultiMap<>(name, channel, vertx);
      } catch (Exception e) {
        throw new VertxException(e);
      }
    }, handler);
  }

  @Override
  public <K, V> void getAsyncMap(String name, MapOptions options, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
  }

  @Override
  public <K, V> Map<K, V> getSyncMap(String name) {
    return null;
  }

  @Override
  public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> handler) {
    vertx.executeBlocking(
        () -> {
          ClusteredLockImpl lock = new ClusteredLockImpl(lockService, name);
          if (lock.acquire(timeout)) {
            if (log.isDebugEnabled()) {
              log.debug(String.format("Lock acquired on [%s]", name));
            }
            return lock;
          } else {
            log.error(String.format("Timed out waiting to get lock [%s]", name));
            throw new VertxException(String.format("Timed out waiting to get lock [%s]", name));
          }
        },
        handler
    );
  }

  @Override
  public void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
    System.out.println("JGroupsClusterManager.getCounter");
    System.out.println("name = [" + name + "], handler = [" + handler + "]");
    vertx.executeBlocking(
        () -> new ClusteredCounterImpl(vertx, counterService.getOrCreateCounter(name, 0L)),
        handler
    );
  }

  @Override
  public String getNodeID() {
    return address;
  }

  @Override
  public List<String> getNodes() {
    return topologyListener.getNodes();
  }

  @Override
  public void nodeListener(NodeListener listener) {
    topologyListener.setNodeListener(listener);
  }

  @Override
  public void join(Handler<AsyncResult<Void>> handler) {
    vertx.executeBlocking(() -> {
      if (active) {
        return null;
      }
      active = true;

      try {
        channel = new JChannel("jgroups-udp.xml");
        topologyListener = new TopologyListener();
        channel.setReceiver(topologyListener);
        channel.connect(CLUSTER_NAME);

        address = channel.getAddressAsString();
        if (log.isInfoEnabled()) {
          log.info(String.format("Node id=%s join the cluster", address));
        }

        counterChannel = new ForkChannel(channel, VERTX_COUNTER_CHANNEL, address, true, ProtocolStack.ABOVE, FRAG2.class, new COUNTER());
        counterChannel.connect(CLUSTER_NAME);
        counterService = new CounterService(counterChannel);

        lockChannel = new ForkChannel(channel, VERTX_LOCK_CHANNEL, address, true, ProtocolStack.ABOVE, FRAG2.class, new VERTX_LOCK());
        lockChannel.connect(CLUSTER_NAME);
        lockService = new LockService(lockChannel);

        return null;
      } catch (Exception e) {
        active = false;
        throw new RuntimeException(e);
      }
    }, handler);
  }

  @Override
  public void leave(Handler<AsyncResult<Void>> handler) {
    vertx.executeBlocking(() -> {
      if (!active) {
        return null;
      }
      active = false;
      if (log.isInfoEnabled()) {
        log.info(String.format("Node id=%s leave the cluster", this.getNodeID()));
      }

      counterChannel.close();
      lockChannel.close();
      channel.close();

      counterChannel = null;
      lockChannel = null;
      channel = null;
      address = null;

      return null;
    }, handler);
  }

  @Override
  public boolean isActive() {
    return active;
  }

}
