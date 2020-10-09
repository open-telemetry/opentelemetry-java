/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringKey;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;

/**
 * Defines constants for all attribute names defined in the OpenTelemetry Semantic Conventions
 * specifications.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/README.md">Semantic
 *     Conventions</a>
 */
public final class SemanticAttributes {

  /** Transport protocol used. */
  public static final AttributeKey<String> NET_TRANSPORT = stringKey("net.transport");
  /** Remote address of the peer (dotted decimal for IPv4 or RFC5952 for IPv6). */
  public static final AttributeKey<String> NET_PEER_IP = stringKey("net.peer.ip");
  /** Remote port number as an integer. E.g., 80. */
  public static final AttributeKey<Long> NET_PEER_PORT = longKey("net.peer.port");
  /** Remote hostname or similar. */
  public static final AttributeKey<String> NET_PEER_NAME = stringKey("net.peer.name");
  /** Like net.peer.ip but for the host IP. Useful in case of a multi-IP host. */
  public static final AttributeKey<String> NET_HOST_IP = stringKey("net.host.ip");
  /** Like net.peer.port but for the host port. */
  public static final AttributeKey<Long> NET_HOST_PORT = longKey("net.host.port");
  /** Local hostname or similar. */
  public static final AttributeKey<String> NET_HOST_NAME = stringKey("net.host.name");

  /** Logical name of a remote service. */
  public static final AttributeKey<String> PEER_SERVICE = stringKey("peer.service");

  /**
   * Username or client_id extracted from the access token or Authorization header in the inbound
   * request from outside the system.
   */
  public static final AttributeKey<String> ENDUSER_ID = stringKey("enduser.id");
  /**
   * Actual/assumed role the client is making the request under extracted from token or application
   * security context.
   */
  public static final AttributeKey<String> ENDUSER_ROLE = stringKey("enduser.role");
  /**
   * Scopes or granted authorities the client currently possesses extracted from token or
   * application security context. The value would come from the scope associated with an OAuth 2.0
   * Access Token or an attribute value in a SAML 2.0 Assertion.
   */
  public static final AttributeKey<String> ENDUSER_SCOPE = stringKey("enduser.scope");
  /** HTTP request method. E.g. "GET". */
  public static final AttributeKey<String> HTTP_METHOD = stringKey("http.method");
  /** Full HTTP request URL in the form scheme://host[:port]/path?query[#fragment]. */
  public static final AttributeKey<String> HTTP_URL = stringKey("http.url");
  /** The full request target as passed in a HTTP request line or equivalent. */
  public static final AttributeKey<String> HTTP_TARGET = stringKey("http.target");
  /** The value of the HTTP host header. */
  public static final AttributeKey<String> HTTP_HOST = stringKey("http.host");
  /** The URI scheme identifying the used protocol: "http" or "https". */
  public static final AttributeKey<String> HTTP_SCHEME = stringKey("http.scheme");
  /** HTTP response status code. E.g. 200 (integer) If and only if one was received/sent. */
  public static final AttributeKey<Long> HTTP_STATUS_CODE = longKey("http.status_code");
  /** Kind of HTTP protocol used: "1.0", "1.1", "2", "SPDY" or "QUIC". */
  public static final AttributeKey<String> HTTP_FLAVOR = stringKey("http.flavor");
  /** Value of the HTTP "User-Agent" header sent by the client. */
  public static final AttributeKey<String> HTTP_USER_AGENT = stringKey("http.user_agent");
  /** The primary server name of the matched virtual host. Usually obtained via configuration. */
  public static final AttributeKey<String> HTTP_SERVER_NAME = stringKey("http.server_name");
  /** The matched route (path template). */
  public static final AttributeKey<String> HTTP_ROUTE = stringKey("http.route");
  /** The IP address of the original client behind all proxies, if known. */
  public static final AttributeKey<String> HTTP_CLIENT_IP = stringKey("http.client_ip");
  /**
   * The size of the request payload body, in bytes. For payloads using transport encoding, this is
   * the compressed size.
   */
  public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH =
      longKey("http.request_content_length");
  /**
   * The size of the uncompressed request payload body, in bytes. Only set for requests that use
   * transport encoding.
   */
  public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED =
      longKey("http.request_content_length_uncompressed");
  /**
   * The size of the response payload body, in bytes. For payloads using transport encoding, this is
   * the compressed size.
   */
  public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH =
      longKey("http.response_content_length");
  /**
   * The size of the uncompressed response payload body, in bytes. Only set for responses that use
   * transport encoding.
   */
  public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED =
      longKey("http.response_content_length_uncompressed");

  /** A string identifying the remoting system, e.g., "grpc", "java_rmi" or "wcf". */
  public static final AttributeKey<String> RPC_SYSTEM = stringKey("rpc.system");
  /** The full name of the service being called, including its package name, if applicable. */
  public static final AttributeKey<String> RPC_SERVICE = stringKey("rpc.service");
  /* The name of the method being called, must be equal to the $method part in the span name */
  public static final AttributeKey<String> RPC_METHOD = stringKey("rpc.method");

  /** The name of a gRPC span event to populate for each message sent / received. */
  public static final String GRPC_MESSAGE_EVENT_NAME = "message";
  /** gRPC span event attribute with value "SENT" or "RECEIVED". */
  public static final AttributeKey<String> GRPC_MESSAGE_TYPE = stringKey("message.type");
  /** gRPC span event attribute starting from 1 for each of sent messages and received messages. */
  public static final AttributeKey<Long> GRPC_MESSAGE_ID = longKey("message.id");
  /** gRPC span event attribute for compressed size of a message. */
  public static final AttributeKey<Long> GRPC_MESSAGE_COMPRESSED_SIZE =
      longKey("message.compressed_size");
  /** gRPC span event attribute for uncompressed size of a message. */
  public static final AttributeKey<Long> GRPC_MESSAGE_UNCOMPRESSED_SIZE =
      longKey("message.uncompressed_size");

  /**
   * An identifier for the database management system (DBMS) product being used.
   *
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/database.md#notes-and-well-known-identifiers-for-dbsystem">A
   *     list of well-known identifiers</a>
   */
  public static final AttributeKey<String> DB_SYSTEM = stringKey("db.system");
  /** Database name. */
  public static final AttributeKey<String> DB_NAME = stringKey("db.name");
  /**
   * The connection string used to connect to the database. It's recommended to remove embedded
   * credentials. This will replace db.url.
   */
  public static final AttributeKey<String> DB_CONNECTION_STRING = stringKey("db.connection_string");
  /** Database statement for the given database type. */
  public static final AttributeKey<String> DB_STATEMENT = stringKey("db.statement");
  /** Database operation that is being executed. */
  public static final AttributeKey<String> DB_OPERATION = stringKey("db.operation");
  /** Username for accessing database. */
  public static final AttributeKey<String> DB_USER = stringKey("db.user");

  /**
   * For db.system == mssql, the instance name connecting to. This name is used to determine the
   * port of a named instance. When set, {@link #NET_PEER_PORT} is not required, but recommended
   * when connecting to a non-standard port.
   */
  public static final AttributeKey<String> MSSQL_SQL_SERVER = stringKey("db.mssql.instance_name");
  /**
   * For JDBC clients, the fully-qualified class name of the Java Database Connectivity (JDBC)
   * driver used to connect, e.g. "org.postgresql.Driver" or
   * "com.microsoft.sqlserver.jdbc.SQLServerDriver".
   */
  public static final AttributeKey<String> JDBC_DRIVER_CLASSNAME =
      stringKey("db.jdbc.driver_classname");

  /**
   * For db.system == cassandra, the name of the keyspace being accessed. To be used instead of the
   * generic db.name attribute.
   */
  public static final AttributeKey<String> CASSANDRA_KEYSPACE = stringKey("db.cassandra.keyspace");
  /**
   * For db.system == hbase, the namespace being accessed. To be used instead of the generic db.name
   * attribute.
   */
  public static final AttributeKey<String> HBASE_NAMESPACE = stringKey("db.hbase.namespace");
  /**
   * For db.system == redis, the index of the database being accessed as used in the SELECT command,
   * provided as an integer. To be used instead of the generic db.name attribute.
   */
  public static final AttributeKey<Long> REDIS_DATABASE_INDEX = longKey("db.redis.database_index");
  /**
   * For db.system == mongodb, the collection being accessed within the database stated in db.name
   */
  public static final AttributeKey<String> MONGODB_COLLECTION = stringKey("db.mongodb.collection");

  /** A string identifying the messaging system such as kafka, rabbitmq or activemq. */
  public static final AttributeKey<String> MESSAGING_SYSTEM = stringKey("messaging.system");
  /**
   * The message destination name, e.g. MyQueue or MyTopic. This might be equal to the span name but
   * is required nevertheless
   */
  public static final AttributeKey<String> MESSAGING_DESTINATION =
      stringKey("messaging.destination");
  /** The kind of message destination. Either queue or topic. */
  public static final AttributeKey<String> MESSAGING_DESTINATION_KIND =
      stringKey("messaging.destination_kind");
  /** A boolean that is true if the message destination is temporary. */
  public static final AttributeKey<Boolean> MESSAGING_TEMP_DESTINATION =
      booleanKey("messaging.temp_destination");
  /** The name of the transport protocol such as AMQP or MQTT. */
  public static final AttributeKey<String> MESSAGING_PROTOCOL = stringKey("messaging.protocol");
  /** The version of the transport protocol such as 0.9.1. */
  public static final AttributeKey<String> MESSAGING_PROTOCOL_VERSION =
      stringKey("messaging.protocol_version");
  /**
   * Connection string such as tibjmsnaming://localhost:7222 or
   * https://queue.amazonaws.com/80398EXAMPLE/MyQueue
   */
  public static final AttributeKey<String> MESSAGING_URL = stringKey("messaging.url");
  /**
   * A value used by the messaging system as an identifier for the message, represented as a string.
   */
  public static final AttributeKey<String> MESSAGING_MESSAGE_ID = stringKey("messaging.message_id");
  /**
   * A value identifying the conversation to which the message belongs, represented as a string.
   * Sometimes called "Correlation ID".
   */
  public static final AttributeKey<String> MESSAGING_CONVERSATION_ID =
      stringKey("messaging.conversation_id");
  /**
   * The (uncompressed) size of the message payload in bytes. Also use this attribute if it is
   * unknown whether the compressed or uncompressed payload size is reported.
   */
  public static final AttributeKey<Long> MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES =
      longKey("messaging.message_payload_size_bytes");
  /** The compressed size of the message payload in bytes. */
  public static final AttributeKey<Long> MESSAGING_MESSAGE_PAYLOAD_COMPRESSED_SIZE_BYTES =
      longKey("messaging.message_payload_compressed_size_bytes");
  /**
   * A string identifying which part and kind of message consumption this span describes: either
   * "receive" or "process". If the operation is "send", this attribute must not be set: the
   * operation can be inferred from the span kind in that case.
   */
  public static final AttributeKey<String> MESSAGING_OPERATION = stringKey("messaging.operation");

  /**
   * The name of an event describing an exception.
   *
   * <p>Typically an event with that name should not be manually created. Instead {@link
   * io.opentelemetry.trace.Span#recordException(Throwable)} should be used.
   */
  public static final String EXCEPTION_EVENT_NAME = "exception";

  /**
   * The type of the exception, i.e., it's fully qualified name (used on exception events).
   *
   * <p>Typically this should not be manually set. Instead {@link
   * io.opentelemetry.trace.Span#recordException(Throwable)} should be used.
   */
  public static final AttributeKey<String> EXCEPTION_TYPE = stringKey("exception.type");

  /**
   * The exception message (used on exception events).
   *
   * <p>Typically this should not be manually set. Instead {@link
   * io.opentelemetry.trace.Span#recordException(Throwable)} should be used.
   */
  public static final AttributeKey<String> EXCEPTION_MESSAGE = stringKey("exception.message");

  /**
   * A string representing the stacktrace of an exception, as produced by {@link
   * Throwable#printStackTrace()} (used on exception events).
   *
   * <p>Typically this should not be manually set. Instead {@link
   * io.opentelemetry.trace.Span#recordException(Throwable)} should be used.
   */
  public static final AttributeKey<String> EXCEPTION_STACKTRACE = stringKey("exception.stacktrace");

  /**
   * A boolean which SHOULD be set to {@code true} if the exception is recorded at a point where it
   * is known that it is escaping the scope of the span (used on exception events).
   *
   * <p>This should usually be used as second argument to {@link
   * io.opentelemetry.trace.Span#recordException(Throwable, Attributes)}.
   */
  public static final AttributeKey<Boolean> EXCEPTION_ESCAPED = booleanKey("exception.escaped");

  /** Id of the thread that has started a span, as produced by {@link Thread#getId()}. */
  public static final AttributeKey<Long> THREAD_ID = longKey("thread.id");
  /** Name of the thread that has started a span, as produced by {@link Thread#getName()}. */
  public static final AttributeKey<String> THREAD_NAME = stringKey("thread.name");

  /** Type of the trigger on which the function is executed. */
  public static final AttributeKey<String> FAAS_TRIGGER = stringKey("faas.trigger");
  /** String containing the execution id of the function. */
  public static final AttributeKey<String> FAAS_EXECUTION = stringKey("faas.execution");
  /** Indicates that the serverless function is executed for the first time (aka cold start). */
  public static final AttributeKey<Boolean> FAAS_COLDSTART = booleanKey("faas.coldstart");
  /** The name of the invoked function. */
  public static final AttributeKey<String> FAAS_INVOKED_NAME = stringKey("faas.invoked_name");
  /** The cloud provider of the invoked function. */
  public static final AttributeKey<String> FAAS_INVOKED_PROVIDER =
      stringKey("faas.invoked_provider");
  /** The cloud region of the invoked function. */
  public static final AttributeKey<String> FAAS_INVOKED_REGION = stringKey("faas.invoked_region");

  /** For faas.trigger == datasource, the name of the source on which the operation was perfomed. */
  public static final AttributeKey<String> FAAS_DOCUMENT_COLLECTION =
      stringKey("faas.document.collection");
  /**
   * For faas.trigger == datasource, describes the type of the operation that was performed on the
   * data.
   */
  public static final AttributeKey<String> FAAS_DOCUMENT_OPERATION =
      stringKey("faas.document.operation");
  /**
   * For faas.trigger == datasource, a string containing the time when the data was accessed in the
   * ISO 8601 format expressed in UTC.
   */
  public static final AttributeKey<String> FAAS_DOCUMENT_TIME = stringKey("faas.document.time");
  /** For faas.trigger == datasource, the document name/table subjected to the operation. */
  public static final AttributeKey<String> FAAS_DOCUMENT_NAME = stringKey("faas.document.name");

  /**
   * For faas.trigger == time, a string containing the function invocation time in the ISO 8601
   * format expressed in UTC.
   */
  public static final AttributeKey<String> FAAS_TIME = stringKey("faas.time");
  /** For faas.trigger == time, a string containing the schedule period as Cron Expression. */
  public static final AttributeKey<String> FAAS_CRON = stringKey("faas.cron");

  private SemanticAttributes() {}
}
