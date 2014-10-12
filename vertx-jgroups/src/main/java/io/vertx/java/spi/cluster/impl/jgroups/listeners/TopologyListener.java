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

import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;
import org.jgroups.Address;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.util.*;
import java.util.stream.Collectors;

public class TopologyListener extends ReceiverAdapter {

  private final static Logger log = LoggerFactory.getLogger(TopologyListener.class);

  private List<Address> oldMembers = Collections.emptyList();
  private Optional<NodeListener> nodeListener;

  public TopologyListener() {
    this.nodeListener = Optional.empty();
  }

  public TopologyListener(NodeListener nodeListener) {
    this.nodeListener = Optional.of(nodeListener);
  }

  @Override
  public void viewAccepted(View view) {
    List<Address> newMembers = view.getMembers();

    nodeListener.ifPresent((listener) -> {
      newMembers.stream()
          .filter((member) -> !oldMembers.contains(member))
          .map(Address::toString)
          .forEach((member) -> {
            if (log.isInfoEnabled()) {
              log.info(String.format("Notify join a new cluster member [%s]", member));
            }
            listener.nodeAdded(member);
          });

      oldMembers.stream()
          .filter((member) -> !newMembers.contains(member))
          .map(Address::toString)
          .forEach((member) -> {
            if (log.isInfoEnabled()) {
              log.info(String.format("Notify removing a cluster member [%s]", member));
            }
            listener.nodeLeft(member);
          });
    });

    oldMembers = newMembers;
  }

  public void setNodeListener(NodeListener nodeListener) {
    this.nodeListener = Optional.of(nodeListener);
  }

  public List<String> getNodes() {
    return oldMembers
        .parallelStream()
        .map(Address::toString)
        .collect(Collectors.toList());
  }
}
