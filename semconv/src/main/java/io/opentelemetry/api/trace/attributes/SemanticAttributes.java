/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.attributes;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

// DO NOT EDIT, this is an Auto-generated file from /templates/SemanticAttributes.java.j2

/**
 * This is the deprecated version of the SemanticAttributes class. It will be removed in the next
 * release.
 *
 * @deprecated Please use the new version of these attributes in {@link
 *     io.opentelemetry.semconv.trace.attributes.SemanticAttributes}.
 */
@Deprecated
public final class SemanticAttributes {

  /** Name of the cloud provider. */
  public static final AttributeKey<String> CLOUD_PROVIDER = stringKey("cloud.provider");

  /** The cloud account ID used to identify different entities. */
  public static final AttributeKey<String> CLOUD_ACCOUNT_ID = stringKey("cloud.account.id");

  /** A specific geographical location where different entities can run. */
  public static final AttributeKey<String> CLOUD_REGION = stringKey("cloud.region");

  /**
   * Zones are a sub set of the region connected through low-latency links.
   *
   * <p>Note: In AWS, this is called availability-zone.
   */
  public static final AttributeKey<String> CLOUD_ZONE = stringKey("cloud.zone");

  /**
   * The Amazon Resource Name (ARN) of an [ECS container
   * instance](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_instances.html).
   */
  public static final AttributeKey<String> AWS_ECS_CONTAINER_ARN =
      stringKey("aws.ecs.container.arn");

  /**
   * The ARN of an [ECS
   * cluster](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html).
   */
  public static final AttributeKey<String> AWS_ECS_CLUSTER_ARN = stringKey("aws.ecs.cluster.arn");

  /**
   * The [launch
   * type](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/launch_types.html) for an ECS
   * task.
   */
  public static final AttributeKey<String> AWS_ECS_LAUNCHTYPE = stringKey("aws.ecs.launchtype");

  /**
   * The ARN of an [ECS task
   * definition](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html).
   */
  public static final AttributeKey<String> AWS_ECS_TASK_ARN = stringKey("aws.ecs.task.arn");

  /** The task definition family this task definition is a member of. */
  public static final AttributeKey<String> AWS_ECS_TASK_FAMILY = stringKey("aws.ecs.task.family");

  /**
   * The name(s) of the AWS log group(s) an application is writing to.
   *
   * <p>Note: Multiple log groups must be supported for cases like multi-container applications,
   * where a single application has sidecar containers, and each write to their own log group.
   */
  public static final AttributeKey<List<String>> AWS_LOG_GROUP_NAMES =
      stringArrayKey("aws.log.group.names");

  /**
   * The Amazon Resource Name(s) (ARN) of the AWS log group(s).
   *
   * <p>Note: See the [log group ARN format
   * documentation](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format).
   */
  public static final AttributeKey<List<String>> AWS_LOG_GROUP_ARNS =
      stringArrayKey("aws.log.group.arns");

  /** The name(s) of the AWS log stream(s) an application is writing to. */
  public static final AttributeKey<List<String>> AWS_LOG_STREAM_NAMES =
      stringArrayKey("aws.log.stream.names");

  /**
   * The ARN(s) of the AWS log stream(s).
   *
   * <p>Note: See the [log stream ARN format
   * documentation](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format).
   * One log group can contain several log streams, so these ARNs necessarily identify both a log
   * group and a log stream.
   */
  public static final AttributeKey<List<String>> AWS_LOG_STREAM_ARNS =
      stringArrayKey("aws.log.stream.arns");

  /** Container name. */
  public static final AttributeKey<String> CONTAINER_NAME = stringKey("container.name");

  /**
   * Container id. Usually a UUID, as for example used to [identify Docker
   * containers](https://docs.docker.com/engine/reference/run/#container-identification). The UUID
   * might be abbreviated.
   */
  public static final AttributeKey<String> CONTAINER_ID = stringKey("container.id");

  /** Name of the image the container was built on. */
  public static final AttributeKey<String> CONTAINER_IMAGE_NAME = stringKey("container.image.name");

  /** Container image tag. */
  public static final AttributeKey<String> CONTAINER_IMAGE_TAG = stringKey("container.image.tag");

  /**
   * Name of the [deployment environment](https://en.wikipedia.org/wiki/Deployment_environment) (aka
   * deployment tier).
   */
  public static final AttributeKey<String> DEPLOYMENT_ENVIRONMENT =
      stringKey("deployment.environment");

  /** The name of the function being executed. */
  public static final AttributeKey<String> FAAS_NAME = stringKey("faas.name");

  /**
   * The unique ID of the function being executed.
   *
   * <p>Note: For example, in AWS Lambda this field corresponds to the
   * [ARN](https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html) value, in GCP
   * to the URI of the resource, and in Azure to the
   * [FunctionDirectory](https://github.com/Azure/azure-functions-host/wiki/Retrieving-information-about-the-currently-running-function)
   * field.
   */
  public static final AttributeKey<String> FAAS_ID = stringKey("faas.id");

  /**
   * The version string of the function being executed as defined in [Version
   * Attributes](../../resource/semantic_conventions/README.md#version-attributes).
   */
  public static final AttributeKey<String> FAAS_VERSION = stringKey("faas.version");

  /** The execution environment ID as a string. */
  public static final AttributeKey<String> FAAS_INSTANCE = stringKey("faas.instance");

  /** Unique host ID. For Cloud, this must be the instance_id assigned by the cloud provider. */
  public static final AttributeKey<String> HOST_ID = stringKey("host.id");

  /**
   * Name of the host. On Unix systems, it may contain what the hostname command returns, or the
   * fully qualified hostname, or another name specified by the user.
   */
  public static final AttributeKey<String> HOST_NAME = stringKey("host.name");

  /** Type of host. For Cloud, this must be the machine type. */
  public static final AttributeKey<String> HOST_TYPE = stringKey("host.type");

  /** Name of the VM image or OS install the host was instantiated from. */
  public static final AttributeKey<String> HOST_IMAGE_NAME = stringKey("host.image.name");

  /** VM image ID. For Cloud, this value is from the provider. */
  public static final AttributeKey<String> HOST_IMAGE_ID = stringKey("host.image.id");

  /**
   * The version string of the VM image as defined in [Version
   * Attributes](README.md#version-attributes).
   */
  public static final AttributeKey<String> HOST_IMAGE_VERSION = stringKey("host.image.version");

  /** The name of the cluster. */
  public static final AttributeKey<String> K8S_CLUSTER_NAME = stringKey("k8s.cluster.name");

  /** The name of the namespace that the pod is running in. */
  public static final AttributeKey<String> K8S_NAMESPACE_NAME = stringKey("k8s.namespace.name");

  /** The UID of the Pod. */
  public static final AttributeKey<String> K8S_POD_UID = stringKey("k8s.pod.uid");

  /** The name of the Pod. */
  public static final AttributeKey<String> K8S_POD_NAME = stringKey("k8s.pod.name");

  /** The name of the Container in a Pod template. */
  public static final AttributeKey<String> K8S_CONTAINER_NAME = stringKey("k8s.container.name");

  /** The UID of the ReplicaSet. */
  public static final AttributeKey<String> K8S_REPLICASET_UID = stringKey("k8s.replicaset.uid");

  /** The name of the ReplicaSet. */
  public static final AttributeKey<String> K8S_REPLICASET_NAME = stringKey("k8s.replicaset.name");

  /** The UID of the Deployment. */
  public static final AttributeKey<String> K8S_DEPLOYMENT_UID = stringKey("k8s.deployment.uid");

  /** The name of the Deployment. */
  public static final AttributeKey<String> K8S_DEPLOYMENT_NAME = stringKey("k8s.deployment.name");

  /** The UID of the StatefulSet. */
  public static final AttributeKey<String> K8S_STATEFULSET_UID = stringKey("k8s.statefulset.uid");

  /** The name of the StatefulSet. */
  public static final AttributeKey<String> K8S_STATEFULSET_NAME = stringKey("k8s.statefulset.name");

  /** The UID of the DaemonSet. */
  public static final AttributeKey<String> K8S_DAEMONSET_UID = stringKey("k8s.daemonset.uid");

  /** The name of the DaemonSet. */
  public static final AttributeKey<String> K8S_DAEMONSET_NAME = stringKey("k8s.daemonset.name");

  /** The UID of the Job. */
  public static final AttributeKey<String> K8S_JOB_UID = stringKey("k8s.job.uid");

  /** The name of the Job. */
  public static final AttributeKey<String> K8S_JOB_NAME = stringKey("k8s.job.name");

  /** The UID of the CronJob. */
  public static final AttributeKey<String> K8S_CRONJOB_UID = stringKey("k8s.cronjob.uid");

  /** The name of the CronJob. */
  public static final AttributeKey<String> K8S_CRONJOB_NAME = stringKey("k8s.cronjob.name");

  /** The operating system type. */
  public static final AttributeKey<String> OS_TYPE = stringKey("os.type");

  /**
   * Human readable (not intended to be parsed) OS version information, like e.g. reported by `ver`
   * or `lsb_release -a` commands.
   */
  public static final AttributeKey<String> OS_DESCRIPTION = stringKey("os.description");

  /** Process identifier (PID). */
  public static final AttributeKey<Long> PROCESS_PID = longKey("process.pid");

  /**
   * The name of the process executable. On Linux based systems, can be set to the `Name` in
   * `proc/[pid]/status`. On Windows, can be set to the base name of `GetProcessImageFileNameW`.
   */
  public static final AttributeKey<String> PROCESS_EXECUTABLE_NAME =
      stringKey("process.executable.name");

  /**
   * The full path to the process executable. On Linux based systems, can be set to the target of
   * `proc/[pid]/exe`. On Windows, can be set to the result of `GetProcessImageFileNameW`.
   */
  public static final AttributeKey<String> PROCESS_EXECUTABLE_PATH =
      stringKey("process.executable.path");

  /**
   * The command used to launch the process (i.e. the command name). On Linux based systems, can be
   * set to the zeroth string in `proc/[pid]/cmdline`. On Windows, can be set to the first parameter
   * extracted from `GetCommandLineW`.
   */
  public static final AttributeKey<String> PROCESS_COMMAND = stringKey("process.command");

  /**
   * The full command used to launch the process as a single string representing the full command.
   * On Windows, can be set to the result of `GetCommandLineW`. Do not set this if you have to
   * assemble it just for monitoring; use `process.command_args` instead.
   */
  public static final AttributeKey<String> PROCESS_COMMAND_LINE = stringKey("process.command_line");

  /**
   * All the command arguments (including the command/executable itself) as received by the process.
   * On Linux-based systems (and some other Unixoid systems supporting procfs), can be set according
   * to the list of null-delimited strings extracted from `proc/[pid]/cmdline`. For libc-based
   * executables, this would be the full argv vector passed to `main`.
   */
  public static final AttributeKey<List<String>> PROCESS_COMMAND_ARGS =
      stringArrayKey("process.command_args");

  /** The username of the user that owns the process. */
  public static final AttributeKey<String> PROCESS_OWNER = stringKey("process.owner");

  /**
   * The name of the runtime of this process. For compiled native binaries, this SHOULD be the name
   * of the compiler.
   */
  public static final AttributeKey<String> PROCESS_RUNTIME_NAME = stringKey("process.runtime.name");

  /**
   * The version of the runtime of this process, as returned by the runtime without modification.
   */
  public static final AttributeKey<String> PROCESS_RUNTIME_VERSION =
      stringKey("process.runtime.version");

  /**
   * An additional description about the runtime of the process, for example a specific vendor
   * customization of the runtime environment.
   */
  public static final AttributeKey<String> PROCESS_RUNTIME_DESCRIPTION =
      stringKey("process.runtime.description");

  /**
   * Logical name of the service.
   *
   * <p>Note: MUST be the same for all instances of horizontally scaled services. If the value was
   * not specified, SDKs MUST fallback to `unknown_service:` concatenated with
   * [`process.executable.name`](process.md#process), e.g. `unknown_service:bash`. If
   * `process.executable.name` is not available, the value MUST be set to `unknown_service`.
   */
  public static final AttributeKey<String> SERVICE_NAME = stringKey("service.name");

  /**
   * A namespace for `service.name`.
   *
   * <p>Note: A string value having a meaning that helps to distinguish a group of services, for
   * example the team name that owns a group of services. `service.name` is expected to be unique
   * within the same namespace. If `service.namespace` is not specified in the Resource then
   * `service.name` is expected to be unique for all services that have no explicit namespace
   * defined (so the empty/unspecified namespace is simply one more valid namespace). Zero-length
   * namespace string is assumed equal to unspecified namespace.
   */
  public static final AttributeKey<String> SERVICE_NAMESPACE = stringKey("service.namespace");

  /**
   * The string ID of the service instance.
   *
   * <p>Note: MUST be unique for each instance of the same `service.namespace,service.name` pair (in
   * other words `service.namespace,service.name,service.id` triplet MUST be globally unique). The
   * ID helps to distinguish instances of the same service that exist at the same time (e.g.
   * instances of a horizontally scaled service). It is preferable for the ID to be persistent and
   * stay the same for the lifetime of the service instance, however it is acceptable that the ID is
   * ephemeral and changes during important lifetime events for the service (e.g. service restarts).
   * If the service has no inherent unique ID that can be used as the value of this attribute it is
   * recommended to generate a random Version 1 or Version 4 RFC 4122 UUID (services aiming for
   * reproducible UUIDs may also use Version 5, see RFC 4122 for more recommendations).
   */
  public static final AttributeKey<String> SERVICE_INSTANCE_ID = stringKey("service.instance.id");

  /** The version string of the service API or implementation. */
  public static final AttributeKey<String> SERVICE_VERSION = stringKey("service.version");

  /** The name of the telemetry SDK as defined above. */
  public static final AttributeKey<String> TELEMETRY_SDK_NAME = stringKey("telemetry.sdk.name");

  /** The language of the telemetry SDK. */
  public static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      stringKey("telemetry.sdk.language");

  /** The version string of the telemetry SDK. */
  public static final AttributeKey<String> TELEMETRY_SDK_VERSION =
      stringKey("telemetry.sdk.version");

  /** The version string of the auto instrumentation agent, if used. */
  public static final AttributeKey<String> TELEMETRY_AUTO_VERSION =
      stringKey("telemetry.auto.version");

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

  /** Remote hostname or similar, see note below. */
  public static final AttributeKey<String> NET_PEER_NAME = stringKey("net.peer.name");

  /**
   * Remote address of the peer (dotted decimal for IPv4 or
   * [RFC5952](https://tools.ietf.org/html/rfc5952) for IPv6).
   */
  public static final AttributeKey<String> NET_PEER_IP = stringKey("net.peer.ip");

  /** Remote port number. */
  public static final AttributeKey<Long> NET_PEER_PORT = longKey("net.peer.port");

  /** Transport protocol used. See note below. */
  public static final AttributeKey<String> NET_TRANSPORT = stringKey("net.transport");

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

  /** HTTP request method. */
  public static final AttributeKey<String> HTTP_METHOD = stringKey("http.method");

  /**
   * Full HTTP request URL in the form `scheme://host[:port]/path?query[#fragment]`. Usually the
   * fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
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

  /** Like `net.peer.ip` but for the host IP. Useful in case of a multi-IP host. */
  public static final AttributeKey<String> NET_HOST_IP = stringKey("net.host.ip");

  /** Like `net.peer.port` but for the host port. */
  public static final AttributeKey<Long> NET_HOST_PORT = longKey("net.host.port");

  /** Local hostname or similar, see note below. */
  public static final AttributeKey<String> NET_HOST_NAME = stringKey("net.host.name");

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

  /**
   * A string identifying the kind of message consumption as defined in the [Operation
   * names](#operation-names) section above. If the operation is &#34;send&#34;, this attribute MUST
   * NOT be set, since the operation can be inferred from the span kind in that case.
   */
  public static final AttributeKey<String> MESSAGING_OPERATION = stringKey("messaging.operation");

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

  // Enum definitions
  public static final class CloudProviderValues {
    /** Amazon Web Services. */
    public static final String AWS = "aws";
    /** Microsoft Azure. */
    public static final String AZURE = "azure";
    /** Google Cloud Platform. */
    public static final String GCP = "gcp";

    private CloudProviderValues() {}
  }

  public enum AwsEcsLaunchtypeValues {
    /** ec2. */
    EC2("EC2"),
    /** fargate. */
    FARGATE("Fargate"),
    ;

    private final String value;

    AwsEcsLaunchtypeValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static final class OsTypeValues {
    /** Microsoft Windows. */
    public static final String WINDOWS = "WINDOWS";
    /** Linux. */
    public static final String LINUX = "LINUX";
    /** Apple Darwin. */
    public static final String DARWIN = "DARWIN";
    /** FreeBSD. */
    public static final String FREEBSD = "FREEBSD";
    /** NetBSD. */
    public static final String NETBSD = "NETBSD";
    /** OpenBSD. */
    public static final String OPENBSD = "OPENBSD";
    /** DragonFly BSD. */
    public static final String DRAGONFLYBSD = "DRAGONFLYBSD";
    /** HP-UX (Hewlett Packard Unix). */
    public static final String HPUX = "HPUX";
    /** AIX (Advanced Interactive eXecutive). */
    public static final String AIX = "AIX";
    /** Oracle Solaris. */
    public static final String SOLARIS = "SOLARIS";
    /** IBM z/OS. */
    public static final String ZOS = "ZOS";

    private OsTypeValues() {}
  }

  public static final class TelemetrySdkLanguageValues {
    /** cpp. */
    public static final String CPP = "cpp";
    /** dotnet. */
    public static final String DOTNET = "dotnet";
    /** erlang. */
    public static final String ERLANG = "erlang";
    /** go. */
    public static final String GO = "go";
    /** java. */
    public static final String JAVA = "java";
    /** nodejs. */
    public static final String NODEJS = "nodejs";
    /** php. */
    public static final String PHP = "php";
    /** python. */
    public static final String PYTHON = "python";
    /** ruby. */
    public static final String RUBY = "ruby";
    /** webjs. */
    public static final String WEBJS = "webjs";

    private TelemetrySdkLanguageValues() {}
  }

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
    public static final String HSQLSB = "hsqlsb";
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

    private DbSystemValues() {}
  }

  public enum NetTransportValues {
    /** IP.TCP. */
    IP_TCP("IP.TCP"),
    /** IP.UDP. */
    IP_UDP("IP.UDP"),
    /** Another IP-based protocol. */
    IP("IP"),
    /** Unix Domain socket. See below. */
    UNIX("Unix"),
    /** Named or anonymous pipe. See note below. */
    PIPE("pipe"),
    /** In-process communication. */
    INPROC("inproc"),
    /** Something else (non IP-based). */
    OTHER("other"),
    ;

    private final String value;

    NetTransportValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public enum DbCassandraConsistencyLevelValues {
    /** ALL. */
    ALL("ALL"),
    /** EACH_QUORUM. */
    EACH_QUORUM("EACH_QUORUM"),
    /** QUORUM. */
    QUORUM("QUORUM"),
    /** LOCAL_QUORUM. */
    LOCAL_QUORUM("LOCAL_QUORUM"),
    /** ONE. */
    ONE("ONE"),
    /** TWO. */
    TWO("TWO"),
    /** THREE. */
    THREE("THREE"),
    /** LOCAL_ONE. */
    LOCAL_ONE("LOCAL_ONE"),
    /** ANY. */
    ANY("ANY"),
    /** SERIAL. */
    SERIAL("SERIAL"),
    /** LOCAL_SERIAL. */
    LOCAL_SERIAL("LOCAL_SERIAL"),
    ;

    private final String value;

    DbCassandraConsistencyLevelValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public enum FaasTriggerValues {
    /** A response to some data source operation such as a database or filesystem read/write. */
    DATASOURCE("datasource"),
    /** To provide an answer to an inbound HTTP request. */
    HTTP("http"),
    /** A function is set to be executed when messages are sent to a messaging system. */
    PUBSUB("pubsub"),
    /** A function is scheduled to be executed regularly. */
    TIMER("timer"),
    /** If none of the others apply. */
    OTHER("other"),
    ;

    private final String value;

    FaasTriggerValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
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

  public enum MessagingDestinationKindValues {
    /** A message sent to a queue. */
    QUEUE("queue"),
    /** A message sent to a topic. */
    TOPIC("topic"),
    ;

    private final String value;

    MessagingDestinationKindValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static final class FaasInvokedProviderValues {
    /** Amazon Web Services. */
    public static final String AWS = "aws";
    /** Amazon Web Services. */
    public static final String AZURE = "azure";
    /** Google Cloud Platform. */
    public static final String GCP = "gcp";

    private FaasInvokedProviderValues() {}
  }

  public enum MessagingOperationValues {
    /** receive. */
    RECEIVE("receive"),
    /** process. */
    PROCESS("process"),
    ;

    private final String value;

    MessagingOperationValues(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public enum RpcGrpcStatusCodeValues {
    /** OK. */
    OK(0),
    /** CANCELLED. */
    CANCELLED(1),
    /** UNKNOWN. */
    UNKNOWN(2),
    /** INVALID_ARGUMENT. */
    INVALID_ARGUMENT(3),
    /** DEADLINE_EXCEEDED. */
    DEADLINE_EXCEEDED(4),
    /** NOT_FOUND. */
    NOT_FOUND(5),
    /** ALREADY_EXISTS. */
    ALREADY_EXISTS(6),
    /** PERMISSION_DENIED. */
    PERMISSION_DENIED(7),
    /** RESOURCE_EXHAUSTED. */
    RESOURCE_EXHAUSTED(8),
    /** FAILED_PRECONDITION. */
    FAILED_PRECONDITION(9),
    /** ABORTED. */
    ABORTED(10),
    /** OUT_OF_RANGE. */
    OUT_OF_RANGE(11),
    /** UNIMPLEMENTED. */
    UNIMPLEMENTED(12),
    /** INTERNAL. */
    INTERNAL(13),
    /** UNAVAILABLE. */
    UNAVAILABLE(14),
    /** DATA_LOSS. */
    DATA_LOSS(15),
    /** UNAUTHENTICATED. */
    UNAUTHENTICATED(16),
    ;

    private final long value;

    RpcGrpcStatusCodeValues(long value) {
      this.value = value;
    }

    public long getValue() {
      return value;
    }
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
