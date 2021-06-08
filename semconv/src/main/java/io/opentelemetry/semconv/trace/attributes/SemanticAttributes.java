/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.semconv.trace.attributes;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/semantic-convention/templates/SemanticAttributes.java.j2
public final class SemanticAttributes {

  /**
   * An identifier for the database management system (DBMS) product being used. See below for a
   * list of well-known identifiers.
   */
  public static final AttributeKey<String> DB_SYSTEM = stringKey("db.system");

  /**
   * The connection string used to connect to the database. It is recommended to remove embedded
   * credentials.
   */
  public static final AttributeKey<String> DB_CONNECTION_STRING = stringKey("db.connection_string");

  /** Username for accessing the database. */
  public static final AttributeKey<String> DB_USER = stringKey("db.user");

  /**
   * The fully-qualified class name of the [Java Database Connectivity
   * (JDBC)](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/) driver used to connect.
   */
  public static final AttributeKey<String> DB_JDBC_DRIVER_CLASSNAME =
      stringKey("db.jdbc.driver_classname");

  /**
   * If no [tech-specific attribute](#call-level-attributes-for-specific-technologies) is defined,
   * this attribute is used to report the name of the database being accessed. For commands that
   * switch the database, this should be set to the target database (even if the command fails).
   *
   * <p>Note: In some SQL databases, the database name to be used is called &#34;schema name&#34;.
   */
  public static final AttributeKey<String> DB_NAME = stringKey("db.name");

  /**
   * The database statement being executed.
   *
   * <p>Note: The value may be sanitized to exclude sensitive information.
   */
  public static final AttributeKey<String> DB_STATEMENT = stringKey("db.statement");

  /**
   * The name of the operation being executed, e.g. the [MongoDB command
   * name](https://docs.mongodb.com/manual/reference/command/#database-operations) such as
   * `findAndModify`, or the SQL keyword.
   *
   * <p>Note: When setting this to an SQL keyword, it is not recommended to attempt any client-side
   * parsing of `db.statement` just to get this property, but it should be set if the operation name
   * is provided by the library being instrumented. If the SQL statement has an ambiguous operation,
   * or performs more than one operation, this value may be omitted.
   */
  public static final AttributeKey<String> DB_OPERATION = stringKey("db.operation");

  /**
   * The Microsoft SQL Server [instance
   * name](https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver15)
   * connecting to. This name is used to determine the port of a named instance.
   *
   * <p>Note: If setting a `db.mssql.instance_name`, `net.peer.port` is no longer required (but
   * still recommended if non-standard).
   */
  public static final AttributeKey<String> DB_MSSQL_INSTANCE_NAME =
      stringKey("db.mssql.instance_name");

  /**
   * The name of the keyspace being accessed. To be used instead of the generic `db.name` attribute.
   */
  public static final AttributeKey<String> DB_CASSANDRA_KEYSPACE =
      stringKey("db.cassandra.keyspace");

  /** The fetch size used for paging, i.e. how many rows will be returned at once. */
  public static final AttributeKey<Long> DB_CASSANDRA_PAGE_SIZE = longKey("db.cassandra.page_size");

  /**
   * The consistency level of the query. Based on consistency values from
   * [CQL](https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/dml/dmlConfigConsistency.html).
   */
  public static final AttributeKey<String> DB_CASSANDRA_CONSISTENCY_LEVEL =
      stringKey("db.cassandra.consistency_level");

  /**
   * The name of the primary table that the operation is acting upon, including the schema name (if
   * applicable).
   *
   * <p>Note: This mirrors the db.sql.table attribute but references cassandra rather than sql. It
   * is not recommended to attempt any client-side parsing of `db.statement` just to get this
   * property, but it should be set if it is provided by the library being instrumented. If the
   * operation is acting upon an anonymous table, or more than one table, this value MUST NOT be
   * set.
   */
  public static final AttributeKey<String> DB_CASSANDRA_TABLE = stringKey("db.cassandra.table");

  /** Whether or not the query is idempotent. */
  public static final AttributeKey<Boolean> DB_CASSANDRA_IDEMPOTENCE =
      booleanKey("db.cassandra.idempotence");

  /**
   * The number of times a query was speculatively executed. Not set or `0` if the query was not
   * executed speculatively.
   */
  public static final AttributeKey<Long> DB_CASSANDRA_SPECULATIVE_EXECUTION_COUNT =
      longKey("db.cassandra.speculative_execution_count");

  /** The ID of the coordinating node for a query. */
  public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_ID =
      stringKey("db.cassandra.coordinator.id");

  /** The data center of the coordinating node for a query. */
  public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_DC =
      stringKey("db.cassandra.coordinator.dc");

  /**
   * The [HBase namespace](https://hbase.apache.org/book.html#_namespace) being accessed. To be used
   * instead of the generic `db.name` attribute.
   */
  public static final AttributeKey<String> DB_HBASE_NAMESPACE = stringKey("db.hbase.namespace");

  /**
   * The index of the database being accessed as used in the [`SELECT`
   * command](https://redis.io/commands/select), provided as an integer. To be used instead of the
   * generic `db.name` attribute.
   */
  public static final AttributeKey<Long> DB_REDIS_DATABASE_INDEX =
      longKey("db.redis.database_index");

  /** The collection being accessed within the database stated in `db.name`. */
  public static final AttributeKey<String> DB_MONGODB_COLLECTION =
      stringKey("db.mongodb.collection");

  /**
   * The name of the primary table that the operation is acting upon, including the schema name (if
   * applicable).
   *
   * <p>Note: It is not recommended to attempt any client-side parsing of `db.statement` just to get
   * this property, but it should be set if it is provided by the library being instrumented. If the
   * operation is acting upon an anonymous table, or more than one table, this value MUST NOT be
   * set.
   */
  public static final AttributeKey<String> DB_SQL_TABLE = stringKey("db.sql.table");

  /**
   * The type of the exception (its fully-qualified class name, if applicable). The dynamic type of
   * the exception should be preferred over the static type in languages that support it.
   */
  public static final AttributeKey<String> EXCEPTION_TYPE = stringKey("exception.type");

  /** The exception message. */
  public static final AttributeKey<String> EXCEPTION_MESSAGE = stringKey("exception.message");

  /**
   * A stacktrace as a string in the natural representation for the language runtime. The
   * representation is to be determined and documented by each language SIG.
   */
  public static final AttributeKey<String> EXCEPTION_STACKTRACE = stringKey("exception.stacktrace");

  /**
   * SHOULD be set to true if the exception event is recorded at a point where it is known that the
   * exception is escaping the scope of the span.
   *
   * <p>Note: An exception is considered to have escaped (or left) the scope of a span, if that span
   * is ended while the exception is still logically &#34;in flight&#34;. This may be actually
   * &#34;in flight&#34; in some languages (e.g. if the exception is passed to a Context
   * manager&#39;s `__exit__` method in Python) but will usually be caught at the point of recording
   * the exception in most languages.
   *
   * <p>It is usually not possible to determine at the point where an exception is thrown whether it
   * will escape the scope of a span. However, it is trivial to know that an exception will escape,
   * if one checks for an active exception just before ending the span, as done in the [example
   * above](#exception-end-example).
   *
   * <p>It follows that an exception may still escape the scope of the span even if the
   * `exception.escaped` attribute was not set or set to false, since the event might have been
   * recorded at a time where it was not clear whether the exception will escape.
   */
  public static final AttributeKey<Boolean> EXCEPTION_ESCAPED = booleanKey("exception.escaped");

  /** Type of the trigger on which the function is executed. */
  public static final AttributeKey<String> FAAS_TRIGGER = stringKey("faas.trigger");

  /** The execution ID of the current function execution. */
  public static final AttributeKey<String> FAAS_EXECUTION = stringKey("faas.execution");

  /**
   * The name of the source on which the triggering operation was performed. For example, in Cloud
   * Storage or S3 corresponds to the bucket name, and in Cosmos DB to the database name.
   */
  public static final AttributeKey<String> FAAS_DOCUMENT_COLLECTION =
      stringKey("faas.document.collection");

  /** Describes the type of the operation that was performed on the data. */
  public static final AttributeKey<String> FAAS_DOCUMENT_OPERATION =
      stringKey("faas.document.operation");

  /**
   * A string containing the time when the data was accessed in the [ISO
   * 8601](https://www.iso.org/iso-8601-date-and-time-format.html) format expressed in
   * [UTC](https://www.w3.org/TR/NOTE-datetime).
   */
  public static final AttributeKey<String> FAAS_DOCUMENT_TIME = stringKey("faas.document.time");

  /**
   * The document name/table subjected to the operation. For example, in Cloud Storage or S3 is the
   * name of the file, and in Cosmos DB the table name.
   */
  public static final AttributeKey<String> FAAS_DOCUMENT_NAME = stringKey("faas.document.name");

  /**
   * A string containing the function invocation time in the [ISO
   * 8601](https://www.iso.org/iso-8601-date-and-time-format.html) format expressed in
   * [UTC](https://www.w3.org/TR/NOTE-datetime).
   */
  public static final AttributeKey<String> FAAS_TIME = stringKey("faas.time");

  /**
   * A string containing the schedule period as [Cron
   * Expression](https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm).
   */
  public static final AttributeKey<String> FAAS_CRON = stringKey("faas.cron");

  /**
   * A boolean that is true if the serverless function is executed for the first time (aka
   * cold-start).
   */
  public static final AttributeKey<Boolean> FAAS_COLDSTART = booleanKey("faas.coldstart");

  /**
   * The name of the invoked function.
   *
   * <p>Note: SHOULD be equal to the `faas.name` resource attribute of the invoked function.
   */
  public static final AttributeKey<String> FAAS_INVOKED_NAME = stringKey("faas.invoked_name");

  /**
   * The cloud provider of the invoked function.
   *
   * <p>Note: SHOULD be equal to the `cloud.provider` resource attribute of the invoked function.
   */
  public static final AttributeKey<String> FAAS_INVOKED_PROVIDER =
      stringKey("faas.invoked_provider");

  /**
   * The cloud region of the invoked function.
   *
   * <p>Note: SHOULD be equal to the `cloud.region` resource attribute of the invoked function.
   */
  public static final AttributeKey<String> FAAS_INVOKED_REGION = stringKey("faas.invoked_region");

  /** Transport protocol used. See note below. */
  public static final AttributeKey<String> NET_TRANSPORT = stringKey("net.transport");

  /**
   * Remote address of the peer (dotted decimal for IPv4 or
   * [RFC5952](https://tools.ietf.org/html/rfc5952) for IPv6).
   */
  public static final AttributeKey<String> NET_PEER_IP = stringKey("net.peer.ip");

  /** Remote port number. */
  public static final AttributeKey<Long> NET_PEER_PORT = longKey("net.peer.port");

  /** Remote hostname or similar, see note below. */
  public static final AttributeKey<String> NET_PEER_NAME = stringKey("net.peer.name");

  /** Like `net.peer.ip` but for the host IP. Useful in case of a multi-IP host. */
  public static final AttributeKey<String> NET_HOST_IP = stringKey("net.host.ip");

  /** Like `net.peer.port` but for the host port. */
  public static final AttributeKey<Long> NET_HOST_PORT = longKey("net.host.port");

  /** Local hostname or similar, see note below. */
  public static final AttributeKey<String> NET_HOST_NAME = stringKey("net.host.name");

  /**
   * The [`service.name`](../../resource/semantic_conventions/README.md#service) of the remote
   * service. SHOULD be equal to the actual `service.name` resource attribute of the remote service
   * if any.
   */
  public static final AttributeKey<String> PEER_SERVICE = stringKey("peer.service");

  /**
   * Username or client_id extracted from the access token or
   * [Authorization](https://tools.ietf.org/html/rfc7235#section-4.2) header in the inbound request
   * from outside the system.
   */
  public static final AttributeKey<String> ENDUSER_ID = stringKey("enduser.id");

  /**
   * Actual/assumed role the client is making the request under extracted from token or application
   * security context.
   */
  public static final AttributeKey<String> ENDUSER_ROLE = stringKey("enduser.role");

  /**
   * Scopes or granted authorities the client currently possesses extracted from token or
   * application security context. The value would come from the scope associated with an [OAuth 2.0
   * Access Token](https://tools.ietf.org/html/rfc6749#section-3.3) or an attribute value in a [SAML
   * 2.0
   * Assertion](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html).
   */
  public static final AttributeKey<String> ENDUSER_SCOPE = stringKey("enduser.scope");

  /** Current &#34;managed&#34; thread ID (as opposed to OS thread ID). */
  public static final AttributeKey<Long> THREAD_ID = longKey("thread.id");

  /** Current thread name. */
  public static final AttributeKey<String> THREAD_NAME = stringKey("thread.name");

  /**
   * The method or function name, or equivalent (usually rightmost part of the code unit&#39;s
   * name).
   */
  public static final AttributeKey<String> CODE_FUNCTION = stringKey("code.function");

  /**
   * The &#34;namespace&#34; within which `code.function` is defined. Usually the qualified class or
   * module name, such that `code.namespace` + some separator + `code.function` form a unique
   * identifier for the code unit.
   */
  public static final AttributeKey<String> CODE_NAMESPACE = stringKey("code.namespace");

  /**
   * The source code file name that identifies the code unit as uniquely as possible (preferably an
   * absolute file path).
   */
  public static final AttributeKey<String> CODE_FILEPATH = stringKey("code.filepath");

  /**
   * The line number in `code.filepath` best representing the operation. It SHOULD point within the
   * code unit named in `code.function`.
   */
  public static final AttributeKey<Long> CODE_LINENO = longKey("code.lineno");

  /** HTTP request method. */
  public static final AttributeKey<String> HTTP_METHOD = stringKey("http.method");

  /**
   * Full HTTP request URL in the form `scheme://host[:port]/path?query[#fragment]`. Usually the
   * fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
   *
   * <p>Note: `http.url` MUST NOT contain credentials passed via URL in form of
   * `https://username:password@www.example.com/`. In such case the attribute&#39;s value should be
   * `https://www.example.com/`.
   */
  public static final AttributeKey<String> HTTP_URL = stringKey("http.url");

  /** The full request target as passed in a HTTP request line or equivalent. */
  public static final AttributeKey<String> HTTP_TARGET = stringKey("http.target");

  /**
   * The value of the [HTTP host header](https://tools.ietf.org/html/rfc7230#section-5.4). When the
   * header is empty or not present, this attribute should be the same.
   */
  public static final AttributeKey<String> HTTP_HOST = stringKey("http.host");

  /** The URI scheme identifying the used protocol. */
  public static final AttributeKey<String> HTTP_SCHEME = stringKey("http.scheme");

  /** [HTTP response status code](https://tools.ietf.org/html/rfc7231#section-6). */
  public static final AttributeKey<Long> HTTP_STATUS_CODE = longKey("http.status_code");

  /**
   * Kind of HTTP protocol used.
   *
   * <p>Note: If `net.transport` is not specified, it can be assumed to be `IP.TCP` except if
   * `http.flavor` is `QUIC`, in which case `IP.UDP` is assumed.
   */
  public static final AttributeKey<String> HTTP_FLAVOR = stringKey("http.flavor");

  /**
   * Value of the [HTTP User-Agent](https://tools.ietf.org/html/rfc7231#section-5.5.3) header sent
   * by the client.
   */
  public static final AttributeKey<String> HTTP_USER_AGENT = stringKey("http.user_agent");

  /**
   * The size of the request payload body in bytes. This is the number of bytes transferred
   * excluding headers and is often, but not always, present as the
   * [Content-Length](https://tools.ietf.org/html/rfc7230#section-3.3.2) header. For requests using
   * transport encoding, this should be the compressed size.
   */
  public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH =
      longKey("http.request_content_length");

  /**
   * The size of the uncompressed request payload body after transport decoding. Not set if
   * transport encoding not used.
   */
  public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED =
      longKey("http.request_content_length_uncompressed");

  /**
   * The size of the response payload body in bytes. This is the number of bytes transferred
   * excluding headers and is often, but not always, present as the
   * [Content-Length](https://tools.ietf.org/html/rfc7230#section-3.3.2) header. For requests using
   * transport encoding, this should be the compressed size.
   */
  public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH =
      longKey("http.response_content_length");

  /**
   * The size of the uncompressed response payload body after transport decoding. Not set if
   * transport encoding not used.
   */
  public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED =
      longKey("http.response_content_length_uncompressed");

  /**
   * The primary server name of the matched virtual host. This should be obtained via configuration.
   * If no such configuration can be obtained, this attribute MUST NOT be set ( `net.host.name`
   * should be used instead).
   *
   * <p>Note: `http.url` is usually not readily available on the server side but would have to be
   * assembled in a cumbersome and sometimes lossy process from other information (see e.g.
   * open-telemetry/opentelemetry-python/pull/148). It is thus preferred to supply the raw data that
   * is available.
   */
  public static final AttributeKey<String> HTTP_SERVER_NAME = stringKey("http.server_name");

  /** The matched route (path template). */
  public static final AttributeKey<String> HTTP_ROUTE = stringKey("http.route");

  /**
   * The IP address of the original client behind all proxies, if known (e.g. from
   * [X-Forwarded-For](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For)).
   *
   * <p>Note: This is not necessarily the same as `net.peer.ip`, which would identify the
   * network-level peer, which may be a proxy.
   */
  public static final AttributeKey<String> HTTP_CLIENT_IP = stringKey("http.client_ip");

  /** The keys in the `RequestItems` object field. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_TABLE_NAMES =
      stringArrayKey("aws.dynamodb.table_names");

  /** The JSON-serialized value of each item in the `ConsumedCapacity` response field. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_CONSUMED_CAPACITY =
      stringArrayKey("aws.dynamodb.consumed_capacity");

  /** The JSON-serialized value of the `ItemCollectionMetrics` response field. */
  public static final AttributeKey<String> AWS_DYNAMODB_ITEM_COLLECTION_METRICS =
      stringKey("aws.dynamodb.item_collection_metrics");

  /** The value of the `ProvisionedThroughput.ReadCapacityUnits` request parameter. */
  public static final AttributeKey<Double> AWS_DYNAMODB_PROVISIONED_READ_CAPACITY =
      doubleKey("aws.dynamodb.provisioned_read_capacity");

  /** The value of the `ProvisionedThroughput.WriteCapacityUnits` request parameter. */
  public static final AttributeKey<Double> AWS_DYNAMODB_PROVISIONED_WRITE_CAPACITY =
      doubleKey("aws.dynamodb.provisioned_write_capacity");

  /** The value of the `ConsistentRead` request parameter. */
  public static final AttributeKey<Boolean> AWS_DYNAMODB_CONSISTENT_READ =
      booleanKey("aws.dynamodb.consistent_read");

  /** The value of the `ProjectionExpression` request parameter. */
  public static final AttributeKey<String> AWS_DYNAMODB_PROJECTION =
      stringKey("aws.dynamodb.projection");

  /** The value of the `Limit` request parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_LIMIT = longKey("aws.dynamodb.limit");

  /** The value of the `AttributesToGet` request parameter. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_ATTRIBUTES_TO_GET =
      stringArrayKey("aws.dynamodb.attributes_to_get");

  /** The value of the `IndexName` request parameter. */
  public static final AttributeKey<String> AWS_DYNAMODB_INDEX_NAME =
      stringKey("aws.dynamodb.index_name");

  /** The value of the `Select` request parameter. */
  public static final AttributeKey<String> AWS_DYNAMODB_SELECT = stringKey("aws.dynamodb.select");

  /** The JSON-serialized value of each item of the `GlobalSecondaryIndexes` request field. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_GLOBAL_SECONDARY_INDEXES =
      stringArrayKey("aws.dynamodb.global_secondary_indexes");

  /** The JSON-serialized value of each item of the `LocalSecondaryIndexes` request field. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_LOCAL_SECONDARY_INDEXES =
      stringArrayKey("aws.dynamodb.local_secondary_indexes");

  /** The value of the `ExclusiveStartTableName` request parameter. */
  public static final AttributeKey<String> AWS_DYNAMODB_EXCLUSIVE_START_TABLE =
      stringKey("aws.dynamodb.exclusive_start_table");

  /** The the number of items in the `TableNames` response parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_TABLE_COUNT =
      longKey("aws.dynamodb.table_count");

  /** The value of the `ScanIndexForward` request parameter. */
  public static final AttributeKey<Boolean> AWS_DYNAMODB_SCAN_FORWARD =
      booleanKey("aws.dynamodb.scan_forward");

  /** The value of the `Segment` request parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_SEGMENT = longKey("aws.dynamodb.segment");

  /** The value of the `TotalSegments` request parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_TOTAL_SEGMENTS =
      longKey("aws.dynamodb.total_segments");

  /** The value of the `Count` response parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_COUNT = longKey("aws.dynamodb.count");

  /** The value of the `ScannedCount` response parameter. */
  public static final AttributeKey<Long> AWS_DYNAMODB_SCANNED_COUNT =
      longKey("aws.dynamodb.scanned_count");

  /** The JSON-serialized value of each item in the `AttributeDefinitions` request field. */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_ATTRIBUTE_DEFINITIONS =
      stringArrayKey("aws.dynamodb.attribute_definitions");

  /**
   * The JSON-serialized value of each item in the the `GlobalSecondaryIndexUpdates` request field.
   */
  public static final AttributeKey<List<String>> AWS_DYNAMODB_GLOBAL_SECONDARY_INDEX_UPDATES =
      stringArrayKey("aws.dynamodb.global_secondary_index_updates");

  /** A string identifying the messaging system. */
  public static final AttributeKey<String> MESSAGING_SYSTEM = stringKey("messaging.system");

  /**
   * The message destination name. This might be equal to the span name but is required
   * nevertheless.
   */
  public static final AttributeKey<String> MESSAGING_DESTINATION =
      stringKey("messaging.destination");

  /** The kind of message destination. */
  public static final AttributeKey<String> MESSAGING_DESTINATION_KIND =
      stringKey("messaging.destination_kind");

  /** A boolean that is true if the message destination is temporary. */
  public static final AttributeKey<Boolean> MESSAGING_TEMP_DESTINATION =
      booleanKey("messaging.temp_destination");

  /** The name of the transport protocol. */
  public static final AttributeKey<String> MESSAGING_PROTOCOL = stringKey("messaging.protocol");

  /** The version of the transport protocol. */
  public static final AttributeKey<String> MESSAGING_PROTOCOL_VERSION =
      stringKey("messaging.protocol_version");

  /** Connection string. */
  public static final AttributeKey<String> MESSAGING_URL = stringKey("messaging.url");

  /**
   * A value used by the messaging system as an identifier for the message, represented as a string.
   */
  public static final AttributeKey<String> MESSAGING_MESSAGE_ID = stringKey("messaging.message_id");

  /**
   * The [conversation ID](#conversations) identifying the conversation to which the message
   * belongs, represented as a string. Sometimes called &#34;Correlation ID&#34;.
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
   * A string identifying the kind of message consumption as defined in the [Operation
   * names](#operation-names) section above. If the operation is &#34;send&#34;, this attribute MUST
   * NOT be set, since the operation can be inferred from the span kind in that case.
   */
  public static final AttributeKey<String> MESSAGING_OPERATION = stringKey("messaging.operation");

  /** RabbitMQ message routing key. */
  public static final AttributeKey<String> MESSAGING_RABBITMQ_ROUTING_KEY =
      stringKey("messaging.rabbitmq.routing_key");

  /**
   * Message keys in Kafka are used for grouping alike messages to ensure they&#39;re processed on
   * the same partition. They differ from `messaging.message_id` in that they&#39;re not unique. If
   * the key is `null`, the attribute MUST NOT be set.
   *
   * <p>Note: If the key type is not string, it&#39;s string representation has to be supplied for
   * the attribute. If the key has no unambiguous, canonical string form, don&#39;t include its
   * value.
   */
  public static final AttributeKey<String> MESSAGING_KAFKA_MESSAGE_KEY =
      stringKey("messaging.kafka.message_key");

  /**
   * Name of the Kafka Consumer Group that is handling the message. Only applies to consumers, not
   * producers.
   */
  public static final AttributeKey<String> MESSAGING_KAFKA_CONSUMER_GROUP =
      stringKey("messaging.kafka.consumer_group");

  /** Client Id for the Consumer or Producer that is handling the message. */
  public static final AttributeKey<String> MESSAGING_KAFKA_CLIENT_ID =
      stringKey("messaging.kafka.client_id");

  /** Partition the message is sent to. */
  public static final AttributeKey<Long> MESSAGING_KAFKA_PARTITION =
      longKey("messaging.kafka.partition");

  /** A boolean that is true if the message is a tombstone. */
  public static final AttributeKey<Boolean> MESSAGING_KAFKA_TOMBSTONE =
      booleanKey("messaging.kafka.tombstone");

  /** A string identifying the remoting system. */
  public static final AttributeKey<String> RPC_SYSTEM = stringKey("rpc.system");

  /** The full name of the service being called, including its package name, if applicable. */
  public static final AttributeKey<String> RPC_SERVICE = stringKey("rpc.service");

  /** The name of the method being called, must be equal to the $method part in the span name. */
  public static final AttributeKey<String> RPC_METHOD = stringKey("rpc.method");

  /**
   * The [numeric status code](https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md) of the
   * gRPC request.
   */
  public static final AttributeKey<Long> RPC_GRPC_STATUS_CODE = longKey("rpc.grpc.status_code");

  /**
   * Protocol version as in `jsonrpc` property of request/response. Since JSON-RPC 1.0 does not
   * specify this, the value can be omitted.
   */
  public static final AttributeKey<String> RPC_JSONRPC_VERSION = stringKey("rpc.jsonrpc.version");

  /**
   * `method` property from request. Unlike `rpc.method`, this may not relate to the actual method
   * being called. Useful for client-side traces since client does not know what will be called on
   * the server.
   */
  public static final AttributeKey<String> RPC_JSONRPC_METHOD = stringKey("rpc.jsonrpc.method");

  /**
   * `id` property of request or response. Since protocol allows id to be int, string, `null` or
   * missing (for notifications), value is expected to be cast to string for simplicity. Use empty
   * string in case of `null` value. Omit entirely if this is a notification.
   */
  public static final AttributeKey<String> RPC_JSONRPC_REQUEST_ID =
      stringKey("rpc.jsonrpc.request_id");

  /** `error.code` property of response if it is an error response. */
  public static final AttributeKey<Long> RPC_JSONRPC_ERROR_CODE = longKey("rpc.jsonrpc.error_code");

  /** `error.message` property of response if it is an error response. */
  public static final AttributeKey<String> RPC_JSONRPC_ERROR_MESSAGE =
      stringKey("rpc.jsonrpc.error_message");

  // Enum definitions
  public static final class DbSystemValues {
    /** Some other SQL database. Fallback only. See notes. */
    public static final String OTHER_SQL = "other_sql";
    /** Microsoft SQL Server. */
    public static final String MSSQL = "mssql";
    /** MySQL. */
    public static final String MYSQL = "mysql";
    /** Oracle Database. */
    public static final String ORACLE = "oracle";
    /** IBM Db2. */
    public static final String DB2 = "db2";
    /** PostgreSQL. */
    public static final String POSTGRESQL = "postgresql";
    /** Amazon Redshift. */
    public static final String REDSHIFT = "redshift";
    /** Apache Hive. */
    public static final String HIVE = "hive";
    /** Cloudscape. */
    public static final String CLOUDSCAPE = "cloudscape";
    /** HyperSQL DataBase. */
    public static final String HSQLDB = "hsqldb";
    /** Progress Database. */
    public static final String PROGRESS = "progress";
    /** SAP MaxDB. */
    public static final String MAXDB = "maxdb";
    /** SAP HANA. */
    public static final String HANADB = "hanadb";
    /** Ingres. */
    public static final String INGRES = "ingres";
    /** FirstSQL. */
    public static final String FIRSTSQL = "firstsql";
    /** EnterpriseDB. */
    public static final String EDB = "edb";
    /** InterSystems Caché. */
    public static final String CACHE = "cache";
    /** Adabas (Adaptable Database System). */
    public static final String ADABAS = "adabas";
    /** Firebird. */
    public static final String FIREBIRD = "firebird";
    /** Apache Derby. */
    public static final String DERBY = "derby";
    /** FileMaker. */
    public static final String FILEMAKER = "filemaker";
    /** Informix. */
    public static final String INFORMIX = "informix";
    /** InstantDB. */
    public static final String INSTANTDB = "instantdb";
    /** InterBase. */
    public static final String INTERBASE = "interbase";
    /** MariaDB. */
    public static final String MARIADB = "mariadb";
    /** Netezza. */
    public static final String NETEZZA = "netezza";
    /** Pervasive PSQL. */
    public static final String PERVASIVE = "pervasive";
    /** PointBase. */
    public static final String POINTBASE = "pointbase";
    /** SQLite. */
    public static final String SQLITE = "sqlite";
    /** Sybase. */
    public static final String SYBASE = "sybase";
    /** Teradata. */
    public static final String TERADATA = "teradata";
    /** Vertica. */
    public static final String VERTICA = "vertica";
    /** H2. */
    public static final String H2 = "h2";
    /** ColdFusion IMQ. */
    public static final String COLDFUSION = "coldfusion";
    /** Apache Cassandra. */
    public static final String CASSANDRA = "cassandra";
    /** Apache HBase. */
    public static final String HBASE = "hbase";
    /** MongoDB. */
    public static final String MONGODB = "mongodb";
    /** Redis. */
    public static final String REDIS = "redis";
    /** Couchbase. */
    public static final String COUCHBASE = "couchbase";
    /** CouchDB. */
    public static final String COUCHDB = "couchdb";
    /** Microsoft Azure Cosmos DB. */
    public static final String COSMOSDB = "cosmosdb";
    /** Amazon DynamoDB. */
    public static final String DYNAMODB = "dynamodb";
    /** Neo4j. */
    public static final String NEO4J = "neo4j";
    /** Apache Geode. */
    public static final String GEODE = "geode";
    /** Elasticsearch. */
    public static final String ELASTICSEARCH = "elasticsearch";
    /** Memcached. */
    public static final String MEMCACHED = "memcached";
    /** CockroachDB. */
    public static final String COCKROACHDB = "cockroachdb";

    private DbSystemValues() {}
  }

  public static final class DbCassandraConsistencyLevelValues {
    /** all. */
    public static final String ALL = "all";
    /** each_quorum. */
    public static final String EACH_QUORUM = "each_quorum";
    /** quorum. */
    public static final String QUORUM = "quorum";
    /** local_quorum. */
    public static final String LOCAL_QUORUM = "local_quorum";
    /** one. */
    public static final String ONE = "one";
    /** two. */
    public static final String TWO = "two";
    /** three. */
    public static final String THREE = "three";
    /** local_one. */
    public static final String LOCAL_ONE = "local_one";
    /** any. */
    public static final String ANY = "any";
    /** serial. */
    public static final String SERIAL = "serial";
    /** local_serial. */
    public static final String LOCAL_SERIAL = "local_serial";

    private DbCassandraConsistencyLevelValues() {}
  }

  public static final class FaasTriggerValues {
    /** A response to some data source operation such as a database or filesystem read/write. */
    public static final String DATASOURCE = "datasource";
    /** To provide an answer to an inbound HTTP request. */
    public static final String HTTP = "http";
    /** A function is set to be executed when messages are sent to a messaging system. */
    public static final String PUBSUB = "pubsub";
    /** A function is scheduled to be executed regularly. */
    public static final String TIMER = "timer";
    /** If none of the others apply. */
    public static final String OTHER = "other";

    private FaasTriggerValues() {}
  }

  public static final class FaasDocumentOperationValues {
    /** When a new object is created. */
    public static final String INSERT = "insert";
    /** When an object is modified. */
    public static final String EDIT = "edit";
    /** When an object is deleted. */
    public static final String DELETE = "delete";

    private FaasDocumentOperationValues() {}
  }

  public static final class FaasInvokedProviderValues {
    /** Amazon Web Services. */
    public static final String AWS = "aws";
    /** Microsoft Azure. */
    public static final String AZURE = "azure";
    /** Google Cloud Platform. */
    public static final String GCP = "gcp";

    private FaasInvokedProviderValues() {}
  }

  public static final class NetTransportValues {
    /** ip_tcp. */
    public static final String IP_TCP = "ip_tcp";
    /** ip_udp. */
    public static final String IP_UDP = "ip_udp";
    /** Another IP-based protocol. */
    public static final String IP = "ip";
    /** Unix Domain socket. See below. */
    public static final String UNIX = "unix";
    /** Named or anonymous pipe. See note below. */
    public static final String PIPE = "pipe";
    /** In-process communication. */
    public static final String INPROC = "inproc";
    /** Something else (non IP-based). */
    public static final String OTHER = "other";

    private NetTransportValues() {}
  }

  public static final class HttpFlavorValues {
    /** HTTP 1.0. */
    public static final String HTTP_1_0 = "1.0";
    /** HTTP 1.1. */
    public static final String HTTP_1_1 = "1.1";
    /** HTTP 2. */
    public static final String HTTP_2_0 = "2.0";
    /** SPDY protocol. */
    public static final String SPDY = "SPDY";
    /** QUIC protocol. */
    public static final String QUIC = "QUIC";

    private HttpFlavorValues() {}
  }

  public static final class MessagingDestinationKindValues {
    /** A message sent to a queue. */
    public static final String QUEUE = "queue";
    /** A message sent to a topic. */
    public static final String TOPIC = "topic";

    private MessagingDestinationKindValues() {}
  }

  public static final class MessagingOperationValues {
    /** receive. */
    public static final String RECEIVE = "receive";
    /** process. */
    public static final String PROCESS = "process";

    private MessagingOperationValues() {}
  }

  public static final class RpcGrpcStatusCodeValues {
    /** OK. */
    public static final long OK = 0;
    /** CANCELLED. */
    public static final long CANCELLED = 1;
    /** UNKNOWN. */
    public static final long UNKNOWN = 2;
    /** INVALID_ARGUMENT. */
    public static final long INVALID_ARGUMENT = 3;
    /** DEADLINE_EXCEEDED. */
    public static final long DEADLINE_EXCEEDED = 4;
    /** NOT_FOUND. */
    public static final long NOT_FOUND = 5;
    /** ALREADY_EXISTS. */
    public static final long ALREADY_EXISTS = 6;
    /** PERMISSION_DENIED. */
    public static final long PERMISSION_DENIED = 7;
    /** RESOURCE_EXHAUSTED. */
    public static final long RESOURCE_EXHAUSTED = 8;
    /** FAILED_PRECONDITION. */
    public static final long FAILED_PRECONDITION = 9;
    /** ABORTED. */
    public static final long ABORTED = 10;
    /** OUT_OF_RANGE. */
    public static final long OUT_OF_RANGE = 11;
    /** UNIMPLEMENTED. */
    public static final long UNIMPLEMENTED = 12;
    /** INTERNAL. */
    public static final long INTERNAL = 13;
    /** UNAVAILABLE. */
    public static final long UNAVAILABLE = 14;
    /** DATA_LOSS. */
    public static final long DATA_LOSS = 15;
    /** UNAUTHENTICATED. */
    public static final long UNAUTHENTICATED = 16;

    private RpcGrpcStatusCodeValues() {}
  }

  // Manually defined and not YET in the YAML
  /**
   * The name of an event describing an exception.
   *
   * <p>Typically an event with that name should not be manually created. Instead {@link
   * io.opentelemetry.api.trace.Span#recordException(Throwable)} should be used.
   */
  public static final String EXCEPTION_EVENT_NAME = "exception";

  private SemanticAttributes() {}
}
