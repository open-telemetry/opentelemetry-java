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
  public static final StringAttributeSetter NET_TRANSPORT =
      StringAttributeSetter.create("net.transport");
  /** Remote address of the peer (dotted decimal for IPv4 or RFC5952 for IPv6). */
  public static final StringAttributeSetter NET_PEER_IP =
      StringAttributeSetter.create("net.peer.ip");
  /** Remote port number as an integer. E.g., 80. */
  public static final LongAttributeSetter NET_PEER_PORT =
      LongAttributeSetter.create("net.peer.port");
  /** Remote hostname or similar. */
  public static final StringAttributeSetter NET_PEER_NAME =
      StringAttributeSetter.create("net.peer.name");
  /** Like net.peer.ip but for the host IP. Useful in case of a multi-IP host. */
  public static final StringAttributeSetter NET_HOST_IP =
      StringAttributeSetter.create("net.host.ip");
  /** Like net.peer.port but for the host port. */
  public static final LongAttributeSetter NET_HOST_PORT =
      LongAttributeSetter.create("net.host.port");
  /** Local hostname or similar. */
  public static final StringAttributeSetter NET_HOST_NAME =
      StringAttributeSetter.create("net.host.name");
  /**
   * Username or client_id extracted from the access token or Authorization header in the inbound
   * request from outside the system.
   */
  public static final StringAttributeSetter ENDUSER_ID = StringAttributeSetter.create("enduser.id");
  /**
   * Actual/assumed role the client is making the request under extracted from token or application
   * security context.
   */
  public static final StringAttributeSetter ENDUSER_ROLE =
      StringAttributeSetter.create("enduser.role");
  /**
   * Scopes or granted authorities the client currently possesses extracted from token or
   * application security context. The value would come from the scope associated with an OAuth 2.0
   * Access Token or an attribute value in a SAML 2.0 Assertion.
   */
  public static final StringAttributeSetter ENDUSER_SCOPE =
      StringAttributeSetter.create("enduser.scope");
  /** HTTP request method. E.g. "GET". */
  public static final StringAttributeSetter HTTP_METHOD =
      StringAttributeSetter.create("http.method");
  /** Full HTTP request URL in the form scheme://host[:port]/path?query[#fragment]. */
  public static final StringAttributeSetter HTTP_URL = StringAttributeSetter.create("http.url");
  /** The full request target as passed in a HTTP request line or equivalent. */
  public static final StringAttributeSetter HTTP_TARGET =
      StringAttributeSetter.create("http.target");
  /** The value of the HTTP host header. */
  public static final StringAttributeSetter HTTP_HOST = StringAttributeSetter.create("http.host");
  /** The URI scheme identifying the used protocol: "http" or "https". */
  public static final StringAttributeSetter HTTP_SCHEME =
      StringAttributeSetter.create("http.scheme");
  /** HTTP response status code. E.g. 200 (integer) If and only if one was received/sent. */
  public static final LongAttributeSetter HTTP_STATUS_CODE =
      LongAttributeSetter.create("http.status_code");
  /** HTTP reason phrase. E.g. "OK" */
  public static final StringAttributeSetter HTTP_STATUS_TEXT =
      StringAttributeSetter.create("http.status_text");
  /** Kind of HTTP protocol used: "1.0", "1.1", "2", "SPDY" or "QUIC". */
  public static final StringAttributeSetter HTTP_FLAVOR =
      StringAttributeSetter.create("http.flavor");
  /** Value of the HTTP "User-Agent" header sent by the client. */
  public static final StringAttributeSetter HTTP_USER_AGENT =
      StringAttributeSetter.create("http.user_agent");
  /** The primary server name of the matched virtual host. Usually obtained via configuration. */
  public static final StringAttributeSetter HTTP_SERVER_NAME =
      StringAttributeSetter.create("http.server_name");
  /** The matched route (path template). */
  public static final StringAttributeSetter HTTP_ROUTE = StringAttributeSetter.create("http.route");
  /** The IP address of the original client behind all proxies, if known. */
  public static final StringAttributeSetter HTTP_CLIENT_IP =
      StringAttributeSetter.create("http.client_ip");
  /** The service name, must be equal to the $service part in the span name. */
  public static final StringAttributeSetter RPC_SERVICE =
      StringAttributeSetter.create("rpc.service");
  /** RPC span event attribute with value "SENT" or "RECEIVED". */
  public static final StringAttributeSetter MESSAGE_TYPE =
      StringAttributeSetter.create("message.type");
  /** RPC span event attribute starting from 1 for each of sent messages and received messages. */
  public static final LongAttributeSetter MESSAGE_ID = LongAttributeSetter.create("message.id");
  /** RPC span event attribute for compressed size. */
  public static final LongAttributeSetter MESSAGE_COMPRESSED_SIZE =
      LongAttributeSetter.create("message.compressed_size");
  /** RPC span event attribute for uncompressed size. */
  public static final LongAttributeSetter MESSAGE_UNCOMPRESSED_SIZE =
      LongAttributeSetter.create("message.uncompressed_size");
  /** Database type. For any SQL database, "sql". For others, the lower-case database category. */
  public static final StringAttributeSetter DB_TYPE = StringAttributeSetter.create("db.type");
  /** Database instance name. */
  public static final StringAttributeSetter DB_INSTANCE =
      StringAttributeSetter.create("db.instance");
  /** Database statement for the given database type. */
  public static final StringAttributeSetter DB_STATEMENT =
      StringAttributeSetter.create("db.statement");
  /** Username for accessing database. */
  public static final StringAttributeSetter DB_USER = StringAttributeSetter.create("db.user");
  /** JDBC substring like "mysql://db.example.com:3306" */
  public static final StringAttributeSetter DB_URL = StringAttributeSetter.create("db.url");

  private SemanticAttributes() {}
}
