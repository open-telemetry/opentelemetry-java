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

  /** Logical name of a remote service. */
  public static final StringAttributeSetter PEER_SERVICE =
      StringAttributeSetter.create("peer.service");

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
  /**
   * The size of the request payload body, in bytes. For payloads using transport encoding, this is
   * the compressed size.
   */
  public static final LongAttributeSetter HTTP_REQUEST_CONTENT_LENGTH =
      LongAttributeSetter.create("http.request_content_length");
  /**
   * The size of the uncompressed request payload body, in bytes. Only set for requests that use
   * transport encoding.
   */
  public static final LongAttributeSetter HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED =
      LongAttributeSetter.create("http.request_content_length_uncompressed");
  /**
   * The size of the response payload body, in bytes. For payloads using transport encoding, this is
   * the compressed size.
   */
  public static final LongAttributeSetter HTTP_RESPONSE_CONTENT_LENGTH =
      LongAttributeSetter.create("http.response_content_length");
  /**
   * The size of the uncompressed response payload body, in bytes. Only set for responses that use
   * transport encoding.
   */
  public static final LongAttributeSetter HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED =
      LongAttributeSetter.create("http.response_content_length_uncompressed");
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
  /**
   * An identifier for the database management system (DBMS) product being used.
   *
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/database.md#notes-and-well-known-identifiers-for-dbsystem">A
   *     list of well-known identifiers</a>
   */
  public static final StringAttributeSetter DB_SYSTEM = StringAttributeSetter.create("db.system");
  /** Database name. */
  public static final StringAttributeSetter DB_NAME = StringAttributeSetter.create("db.name");
  /**
   * The connection string used to connect to the database. It's recommended to remove embedded
   * credentials. This will replace db.url.
   */
  public static final StringAttributeSetter DB_CONNECTION_STRING =
      StringAttributeSetter.create("db.connection_string");
  /** Database statement for the given database type. */
  public static final StringAttributeSetter DB_STATEMENT =
      StringAttributeSetter.create("db.statement");
  /** Database operation that is being executed. */
  public static final StringAttributeSetter DB_OPERATION =
      StringAttributeSetter.create("db.operation");
  /** Username for accessing database. */
  public static final StringAttributeSetter DB_USER = StringAttributeSetter.create("db.user");

  /**
   * For db.system == mssql, the instance name connecting to. This name is used to determine the
   * port of a named instance. When set, {@link #NET_PEER_PORT} is not required, but recommended
   * when connecting to a non-standard port.
   */
  public static final StringAttributeSetter MSSQL_SQL_SERVER =
      StringAttributeSetter.create("db.mssql.instance_name");
  /**
   * For JDBC clients, the fully-qualified class name of the Java Database Connectivity (JDBC)
   * driver used to connect, e.g. "org.postgresql.Driver" or
   * "com.microsoft.sqlserver.jdbc.SQLServerDriver".
   */
  public static final StringAttributeSetter JDBC_DRIVER_CLASSNAME =
      StringAttributeSetter.create("db.jdbc.driver_classname");

  /**
   * For db.system == cassandra, the name of the keyspace being accessed. To be used instead of the
   * generic db.name attribute.
   */
  public static final StringAttributeSetter CASSANDRA_NAMESPACE =
      StringAttributeSetter.create("db.cassandra.keyspace");
  /**
   * For db.system == hbase, the namespace being accessed. To be used instead of the generic db.name
   * attribute.
   */
  public static final StringAttributeSetter HBASE_NAMESPACE =
      StringAttributeSetter.create("db.hbase.namespace");
  /**
   * For db.system == redis, the index of the database being accessed as used in the SELECT command,
   * provided as an integer. To be used instead of the generic db.name attribute.
   */
  public static final StringAttributeSetter REDIS_DATABASE_INDEX =
      StringAttributeSetter.create("db.redis.database_index");
  /**
   * For db.system == mongodb, the collection being accessed within the database stated in db.name
   */
  public static final StringAttributeSetter MONGODB_COLLECTION =
      StringAttributeSetter.create("db.mongodb.collection");

  /** A string identifying the messaging system such as kafka, rabbitmq or activemq. */
  public static final StringAttributeSetter MESSAGING_SYSTEM =
      StringAttributeSetter.create("messaging.system");
  /**
   * The message destination name, e.g. MyQueue or MyTopic. This might be equal to the span name but
   * is required nevertheless
   */
  public static final StringAttributeSetter MESSAGING_DESTINATION =
      StringAttributeSetter.create("messaging.destination");
  /** The kind of message destination. Either queue or topic. */
  public static final StringAttributeSetter MESSAGING_DESTINATION_KIND =
      StringAttributeSetter.create("messaging.destination_kind");
  /** A boolean that is true if the message destination is temporary. */
  public static final BooleanAttributeSetter MESSAGING_TEMP_DESTINATION =
      BooleanAttributeSetter.create("messaging.temp_destination");
  /** The name of the transport protocol such as AMQP or MQTT. */
  public static final StringAttributeSetter MESSAGING_PROTOCOL =
      StringAttributeSetter.create("messaging.protocol");
  /** The version of the transport protocol such as 0.9.1. */
  public static final StringAttributeSetter MESSAGING_PROTOCOL_VERSION =
      StringAttributeSetter.create("messaging.protocol_version");
  /**
   * Connection string such as tibjmsnaming://localhost:7222 or
   * https://queue.amazonaws.com/80398EXAMPLE/MyQueue
   */
  public static final StringAttributeSetter MESSAGING_URL =
      StringAttributeSetter.create("messaging.url");
  /**
   * A value used by the messaging system as an identifier for the message, represented as a string.
   */
  public static final StringAttributeSetter MESSAGING_MESSAGE_ID =
      StringAttributeSetter.create("messaging.message_id");
  /**
   * A value identifying the conversation to which the message belongs, represented as a string.
   * Sometimes called "Correlation ID".
   */
  public static final StringAttributeSetter MESSAGING_CONVERSATION_ID =
      StringAttributeSetter.create("messaging.conversation_id");
  /**
   * The (uncompressed) size of the message payload in bytes. Also use this attribute if it is
   * unknown whether the compressed or uncompressed payload size is reported.
   */
  public static final LongAttributeSetter MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES =
      LongAttributeSetter.create("messaging.message_payload_size_bytes");
  /** The compressed size of the message payload in bytes. */
  public static final LongAttributeSetter MESSAGING_MESSAGE_PAYLOAD_COMPRESSED_SIZE_BYTES =
      LongAttributeSetter.create("messaging.message_payload_compressed_size_bytes");
  /**
   * A string identifying which part and kind of message consumption this span describes: either
   * "receive" or "process". If the operation is "send", this attribute must not be set: the
   * operation can be inferred from the span kind in that case.
   */
  public static final StringAttributeSetter MESSAGING_OPERATION =
      StringAttributeSetter.create("messaging.operation");

  /** The name of an {@link io.opentelemetry.trace.Event} describing an exception. */
  public static final String EXCEPTION_EVENT_NAME = "exception";
  /** The type of the exception, i.e., it's fully qualified name. */
  public static final StringAttributeSetter EXCEPTION_TYPE =
      StringAttributeSetter.create("exception.type");
  /** The exception message. */
  public static final StringAttributeSetter EXCEPTION_MESSAGE =
      StringAttributeSetter.create("exception.message");
  /**
   * A string representing the stacktrace of an exception, as produced by {@link
   * Throwable#printStackTrace()}.
   */
  public static final StringAttributeSetter EXCEPTION_STACKTRACE =
      StringAttributeSetter.create("exception.stacktrace");

  private SemanticAttributes() {}
}
