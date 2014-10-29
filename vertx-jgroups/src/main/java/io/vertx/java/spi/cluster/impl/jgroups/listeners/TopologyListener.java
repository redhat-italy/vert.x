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

package io.vertx.java.spi.cluster.impl.jgroups.listeners;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.java.spi.cluster.impl.jgroups.support.LambdaLogger;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TopologyListener extends ReceiverAdapter implements LambdaLogger {

  private final static Logger LOG = LoggerFactory.getLogger(TopologyListener.class);
  private final String name;

  private List<Address> oldMembers = Collections.emptyList();
  private Optional<NodeListener> nodeListener = Optional.empty();

  public TopologyListener(String name) {
    logDebug(() -> "[" + name + "] - Start topology listener");
    this.name = name;
  }

  @Override
  public void receive(Message msg) {
    System.out.println("[" + name + "] Message receive [" + msg + "]");
  }

  @Override
  public void viewAccepted(View view) {
    List<Address> newMembers = view.getMembers();

    logDebug(() -> "[" + name + "] - View accepted [" + view + "] old view [" + oldMembers + "]");
    System.out.println("[" + name + "] - [" + nodeListener + "]");
    nodeListener.ifPresent((listener) -> {
      newMembers.stream()
          .filter((member) -> !oldMembers.contains(member))
          .map(Address::toString)
          .peek((member) -> logInfo(() -> String.format("[" + name + "] - Notify join a new cluster member [%s]", member)))
          .forEach(listener::nodeAdded);

      oldMembers.stream()
          .filter((member) -> !newMembers.contains(member))
          .map(Address::toString)
          .peek((member) -> logInfo(() -> String.format("[" + name + "] - Notify removing a cluster member [%s]", member)))
          .forEach(listener::nodeLeft);
    });

    oldMembers = new ArrayList<>(newMembers);
  }

  public void setNodeListener(NodeListener nodeListener) {
    logDebug(() -> String.format("[" + name + "] - Set topology listener [%s]", nodeListener));
    this.nodeListener = Optional.of(nodeListener);
  }

  public List<String> getNodes() {
    List<String> result = oldMembers
        .stream()
        .map(Address::toString)
        .collect(Collectors.toList());
    logDebug(() -> String.format("[" + name + "] - Get Nodes from topology [%s]", result));
    return result;
  }

  @Override
  public Logger log() {
    return LOG;
  }
}
