/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.common;

public class SemanticAttributes {
  public static final AttributeKey.StringValuedKey HTTP_METHOD =
      AttributeKey.stringKey("http.method");
  public static final AttributeKey.StringValuedKey HTTP_TARGET =
      AttributeKey.stringKey("http.target");
  public static final AttributeKey.StringValuedKey HTTP_HOST = AttributeKey.stringKey("http.host");
  public static final AttributeKey.StringValuedKey HTTP_SCHEME =
      AttributeKey.stringKey("http.scheme");
  public static final AttributeKey.StringValuedKey NET_PEER_IP =
      AttributeKey.stringKey("net.peer.ip");
  public static final AttributeKey.LongValuedKey NET_PEER_PORT =
      AttributeKey.longKey("net.peer.port");
  public static final AttributeKey.StringValuedKey NET_HOST_IP =
      AttributeKey.stringKey("net.host.ip");
  public static final AttributeKey.LongValuedKey NET_HOST_PORT =
      AttributeKey.longKey("net.host.port");

  private SemanticAttributes() {}
}
