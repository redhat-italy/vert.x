/*
 * Copyright 2014 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version 2.0
 *   (the "License"); you may not use this file except in compliance with the
 *   License.  You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 */

package io.vertx.core.eventbus;

import io.vertx.core.buffer.Buffer;

/**
 *
 * Instances of this class must be stateless as they will be used concurrently.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface MessageCodec<S, R> {

  // Called when object is encoded to wire
  void encodeToWire(Buffer buffer, S s);

  // Called when object is decoded from wire
  R decodeFromWire(int pos, Buffer buffer);

  // Used when sending locally and no wire involved
  // Must, at least, make a copy of the message if it is not immutable
  R transform(S s);

  String name();

  byte systemCodecID();
}
