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

package io.opentelemetry.trace.attributes;

/**
 * Defines constants for all attribute names defined in the OpenTelemetry Semantic Conventions
 * specifications.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/data-semantic-conventions.md">Semantic
 *     Conventions</a>
 */
public final class SemanticAttributes {

  /** Transport protocol used. */
  public static final Attribute<String> NET_TRANSPORT = new StringAttribute("net.transport");
  /** Remote address of the peer (dotted decimal for IPv4 or RFC5952 for IPv6). */
  public static final Attribute<String> NET_PEER_IP = new StringAttribute("net.peer.ip");
  /** Remote port number as an integer. E.g., 80. */
  public static final Attribute<Integer> NET_PEER_PORT = new IntOrStringAttribute("net.peer.port");
  /** Remote hostname or similar. */
  public static final Attribute<String> NET_PEER_NAME = new StringAttribute("net.peer.name");
  /** Like net.peer.ip but for the host IP. Useful in case of a multi-IP host. */
  public static final Attribute<String> NET_HOST_IP = new StringAttribute("net.host.ip");
  /** Like net.peer.port but for the host port. */
  public static final Attribute<Integer> NET_HOST_PORT = new IntOrStringAttribute("net.host.port");
  /** Local hostname or similar. */
  public static final Attribute<String> NET_HOST_NAME = new StringAttribute("net.host.name");
  /** Username or client_id extracted from the access token or Authorization header. */
  public static final Attribute<String> ENDUSER_ID = new StringAttribute("enduser.id");
  /** Actual/assumed role the client is making the request under. */
  public static final Attribute<String> ENDUSER_ROLE = new StringAttribute("enduser.role");
  /** Scopes or granted authorities the client currently possesses. */
  public static final Attribute<String> ENDUSER_SCOPE = new StringAttribute("enduser.scope");
  /** HTTP request method. E.g. "GET". */
  public static final Attribute<String> HTTP_METHOD = new StringAttribute("http.method");
  /** Full HTTP request URL in the form scheme://host[:port]/path?query[#fragment]. */
  public static final Attribute<String> HTTP_URL = new StringAttribute("http.url");
  /** The full request target as passed in a HTTP request line or equivalent. */
  public static final Attribute<String> HTTP_TARGET = new StringAttribute("http.target");
  /** The value of the HTTP host header. */
  public static final Attribute<String> HTTP_HOST = new StringAttribute("http.host");
  /** The URI scheme identifying the used protocol: "http" or "https". */
  public static final Attribute<String> HTTP_SCHEME = new StringAttribute("http.scheme");
  /** HTTP response status code. E.g. 200 (integer) If and only if one was received/sent. */
  public static final Attribute<Integer> HTTP_STATUS_CODE =
      new IntOrStringAttribute("http.status_code");
  /** HTTP reason phrase. E.g. "OK" */
  public static final Attribute<String> HTTP_STATUS_TEXT = new StringAttribute("http.status_text");
  /** Kind of HTTP protocol used: "1.0", "1.1", "2", "SPDY" or "QUIC". */
  public static final Attribute<String> HTTP_FLAVOR = new StringAttribute("http.flavor");
  /** Value of the HTTP "User-Agent" header sent by the client. */
  public static final Attribute<String> HTTP_USER_AGENT = new StringAttribute("http.user_agent");
  /** The primary server name of the matched virtual host. Usually obtained via configuration. */
  public static final Attribute<String> HTTP_SERVER_NAME = new StringAttribute("http.server_name");
  /** The matched route (path template). */
  public static final Attribute<String> HTTP_ROUTE = new StringAttribute("http.route");
  /** The IP address of the original client behind all proxies, if known. */
  public static final Attribute<String> HTTP_CLIENT_IP = new StringAttribute("http.client_ip");
  /** The service name, must be equal to the $service part in the span name. */
  public static final Attribute<String> RPC_SERVICE = new StringAttribute("rpc.service");
  /** RPC span event attribute with value "SENT" or "RECEIVED". */
  public static final Attribute<String> MESSAGE_TYPE = new StringAttribute("message.type");
  /** RPC span event attribute starting from 1 for each of sent messages and received messages. */
  public static final Attribute<Long> MESSAGE_ID = new LongAttribute("message.id");
  /** RPC span event attribute for compressed size. */
  public static final Attribute<Long> MESSAGE_COMPRESSED_SIZE =
      new LongAttribute("message.compressed_size");
  /** RPC span event attribute for uncompressed size. */
  public static final Attribute<Long> MESSAGE_UNCOMPRESSED_SIZE =
      new LongAttribute("message.uncompressed_size");
  /** Database type. For any SQL database, "sql". For others, the lower-case database category. */
  public static final Attribute<String> DB_TYPE = new StringAttribute("db.type");
  /** Database instance name. */
  public static final Attribute<String> DB_ISNTANCE = new StringAttribute("db.instance");
  /** Database statement for the given database type. */
  public static final Attribute<String> DB_STATEMENT = new StringAttribute("db.statement");
  /** Username for accessing database. */
  public static final Attribute<String> DB_USER = new StringAttribute("db.user");
  /** JDBC substring like "mysql://db.example.com:3306" */
  public static final Attribute<String> DB_URL = new StringAttribute("db.url");

  private SemanticAttributes() {}
}
