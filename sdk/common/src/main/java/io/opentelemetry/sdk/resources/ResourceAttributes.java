/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

/**
 * Provides constants for resource semantic conventions defined by the OpenTelemetry specification.
 *
 * @deprecated Please use the generated class in the `opentelemetry-semconv` module.
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/resource/semantic_conventions/README.md">Resource
 *     Conventions</a>
 */
@Deprecated
public final class ResourceAttributes {

  /** The operating system type, such as {@code "WINDOWS"}, {@code "DARWIN"}, {@code "LINUX"}. */
  public static final AttributeKey<String> OS_NAME = stringKey("os.name");

  /**
   * Human readable information about the OS version, e.g. {@code "Microsoft Windows [Version
   * 10.0.18363.778]"}, {@code "Ubuntu 18.04.1 LTS"}.
   */
  public static final AttributeKey<String> OS_DESCRIPTION = stringKey("os.description");

  /** Process identifier (PID). */
  public static final AttributeKey<Long> PROCESS_PID = longKey("process.pid");

  /** The name of the process executable. */
  public static final AttributeKey<String> PROCESS_EXECUTABLE_NAME =
      stringKey("process.executable.name");

  /** The full path to the process executable. */
  public static final AttributeKey<String> PROCESS_EXECUTABLE_PATH =
      stringKey("process.executable.path");

  /** The command used to launch the process (i.e. the command name). */
  public static final AttributeKey<String> PROCESS_COMMAND = stringKey("process.command");

  /**
   * The full command used to launch the process. The value can be either a list of strings
   * representing the ordered list of arguments, or a single string representing the full command.
   */
  public static final AttributeKey<String> PROCESS_COMMAND_LINE = stringKey("process.command_line");

  /** The username of the user that owns the process. */
  public static final AttributeKey<String> PROCESS_OWNER = stringKey("process.owner");

  // TODO: these should be removed once SemanticAttributes contain process.runtime.*
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
   * Logical name of the service. MUST be the same for all instances of horizontally scaled
   * services.
   */
  public static final AttributeKey<String> SERVICE_NAME = stringKey("service.name");
  /**
   * A namespace for `service.name`. A string value having a meaning that helps to distinguish a
   * group of services,
   */
  public static final AttributeKey<String> SERVICE_NAMESPACE = stringKey("service.namespace");
  /**
   * The string ID of the service instance. MUST be unique for each instance of the same
   * `service.namespace,service.name` pair.
   */
  public static final AttributeKey<String> SERVICE_INSTANCE = stringKey("service.instance.id");
  /** The version string of the service API or implementation. */
  public static final AttributeKey<String> SERVICE_VERSION = stringKey("service.version");
  /** The name of the telemetry library. */
  public static final AttributeKey<String> LIBRARY_NAME = stringKey("library.name");
  /** The language of telemetry library and of the code instrumented with it. */
  public static final AttributeKey<String> LIBRARY_LANGUAGE = stringKey("library.language");
  /** The version string of the library. */
  public static final AttributeKey<String> LIBRARY_VERSION = stringKey("library.version");
  /** Container name. */
  public static final AttributeKey<String> CONTAINER_NAME = stringKey("container.name");
  /** Container id. */
  public static final AttributeKey<String> CONTAINER_ID = stringKey("container.id");
  /** Name of the image the container was built on. */
  public static final AttributeKey<String> CONTAINER_IMAGE_NAME = stringKey("container.image.name");
  /** Container image tag. */
  public static final AttributeKey<String> CONTAINER_IMAGE_TAG = stringKey("container.image.tag");
  /** The name of the cluster that the pod is running in. */
  public static final AttributeKey<String> K8S_CLUSTER = stringKey("k8s.cluster.name");
  /** The name of the namespace that the pod is running in. */
  public static final AttributeKey<String> K8S_NAMESPACE = stringKey("k8s.namespace.name");
  /** The name of the pod. */
  public static final AttributeKey<String> K8S_POD = stringKey("k8s.pod.name");
  /** The name of the deployment. */
  public static final AttributeKey<String> K8S_DEPLOYMENT = stringKey("k8s.deployment.name");
  /** Hostname of the host. It contains what the `hostname` command returns on the host machine. */
  public static final AttributeKey<String> HOST_HOSTNAME = stringKey("host.hostname");
  /** Unique host id. For Cloud this must be the instance_id assigned by the cloud provider. */
  public static final AttributeKey<String> HOST_ID = stringKey("host.id");
  /**
   * Name of the host. It may contain what `hostname` returns on Unix systems, the fully qualified,
   * or a name specified by the user.
   */
  public static final AttributeKey<String> HOST_NAME = stringKey("host.name");
  /** Type of host. For Cloud this must be the machine type. */
  public static final AttributeKey<String> HOST_TYPE = stringKey("host.type");
  /** Name of the VM image or OS install the host was instantiated from. */
  public static final AttributeKey<String> HOST_IMAGE_NAME = stringKey("host.image.name");
  /** VM image id. For Cloud, this value is from the provider. */
  public static final AttributeKey<String> HOST_IMAGE_ID = stringKey("host.image.id");
  /** The version string of the VM image. */
  public static final AttributeKey<String> HOST_IMAGE_VERSION = stringKey("host.image.version");
  /** Name of the cloud provider. */
  public static final AttributeKey<String> CLOUD_PROVIDER = stringKey("cloud.provider");
  /** The cloud account id used to identify different entities. */
  public static final AttributeKey<String> CLOUD_ACCOUNT = stringKey("cloud.account.id");
  /** A specific geographical location where different entities can run. */
  public static final AttributeKey<String> CLOUD_REGION = stringKey("cloud.region");
  /** Zones are a sub set of the region connected through low-latency links. */
  public static final AttributeKey<String> CLOUD_ZONE = stringKey("cloud.zone");

  /** The name of the function being executed. */
  public static final AttributeKey<String> FAAS_NAME = stringKey("faas.name");
  /** The unique ID of the function being executed. */
  public static final AttributeKey<String> FAAS_ID = stringKey("faas.id");
  /** The version string of the function being executed. */
  public static final AttributeKey<String> FAAS_VERSION = stringKey("faas.version");
  /** The execution environment ID as a string. */
  public static final AttributeKey<String> FAAS_INSTANCE = stringKey("faas.instance");

  /** The name of the telemetry SDK as defined above. */
  public static final AttributeKey<String> SDK_NAME = stringKey("telemetry.sdk.name");
  /** The language of the telemetry SDK. */
  public static final AttributeKey<String> SDK_LANGUAGE = stringKey("telemetry.sdk.language");
  /** The version string of the telemetry SDK. */
  public static final AttributeKey<String> SDK_VERSION = stringKey("telemetry.sdk.version");

  public static final Attributes FALLBACK_MANDATORY_ATTRIBUTES =
      Attributes.of(SERVICE_NAME, "unknown_service:java");

  private ResourceAttributes() {}
}
