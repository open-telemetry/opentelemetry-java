/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.trace.attributes.LongAttributeSetter;
import io.opentelemetry.trace.attributes.StringAttributeSetter;

/**
 * Provides constants for resource semantic conventions defined by the OpenTelemetry specification.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/resource/semantic_conventions/README.md">Resource
 *     Conventions</a>
 */
public final class ResourceAttributes {

  /** The operating system type, such as {@code "WINDOWS"}, {@code "DARWIN"}, {@code "LINUX"}. */
  public static final StringAttributeSetter OS_NAME = StringAttributeSetter.create("os.name");

  /**
   * Human readable information about the OS version, e.g. {@code "Microsoft Windows [Version
   * 10.0.18363.778]"}, {@code "Ubuntu 18.04.1 LTS"}.
   */
  public static final StringAttributeSetter OS_DESCRIPTION =
      StringAttributeSetter.create("os.description");

  /** Process identifier (PID). */
  public static final LongAttributeSetter PROCESS_PID = LongAttributeSetter.create("process.pid");

  /** The name of the process executable. */
  public static final StringAttributeSetter PROCESS_EXECUTABLE_NAME =
      StringAttributeSetter.create("process.executable.name");

  /** The full path to the process executable. */
  public static final StringAttributeSetter PROCESS_EXECUTABLE_PATH =
      StringAttributeSetter.create("process.executable.path");

  /** The command used to launch the process (i.e. the command name). */
  public static final StringAttributeSetter PROCESS_COMMAND =
      StringAttributeSetter.create("process.command");

  /**
   * The full command used to launch the process. The value can be either a list of strings
   * representing the ordered list of arguments, or a single string representing the full command.
   */
  public static final StringAttributeSetter PROCESS_COMMAND_LINE =
      StringAttributeSetter.create("process.command_line");

  /** The username of the user that owns the process. */
  public static final StringAttributeSetter PROCESS_OWNER =
      StringAttributeSetter.create("process.owner");

  /**
   * Logical name of the service. MUST be the same for all instances of horizontally scaled
   * services.
   */
  public static final StringAttributeSetter SERVICE_NAME =
      StringAttributeSetter.create("service.name");
  /**
   * A namespace for `service.name`. A string value having a meaning that helps to distinguish a
   * group of services,
   */
  public static final StringAttributeSetter SERVICE_NAMESPACE =
      StringAttributeSetter.create("service.namespace");
  /**
   * The string ID of the service instance. MUST be unique for each instance of the same
   * `service.namespace,service.name` pair.
   */
  public static final StringAttributeSetter SERVICE_INSTANCE =
      StringAttributeSetter.create("service.instance.id");
  /** The version string of the service API or implementation. */
  public static final StringAttributeSetter SERVICE_VERSION =
      StringAttributeSetter.create("service.version");
  /** The name of the telemetry library. */
  public static final StringAttributeSetter LIBRARY_NAME =
      StringAttributeSetter.create("library.name");
  /** The language of telemetry library and of the code instrumented with it. */
  public static final StringAttributeSetter LIBRARY_LANGUAGE =
      StringAttributeSetter.create("library.language");
  /** The version string of the library. */
  public static final StringAttributeSetter LIBRARY_VERSION =
      StringAttributeSetter.create("library.version");
  /** Container name. */
  public static final StringAttributeSetter CONTAINER_NAME =
      StringAttributeSetter.create("container.name");
  /** Container id. */
  public static final StringAttributeSetter CONTAINER_ID =
      StringAttributeSetter.create("container.id");
  /** Name of the image the container was built on. */
  public static final StringAttributeSetter CONTAINER_IMAGE_NAME =
      StringAttributeSetter.create("container.image.name");
  /** Container image tag. */
  public static final StringAttributeSetter CONTAINER_IMAGE_TAG =
      StringAttributeSetter.create("container.image.tag");
  /** The name of the cluster that the pod is running in. */
  public static final StringAttributeSetter K8S_CLUSTER =
      StringAttributeSetter.create("k8s.cluster.name");
  /** The name of the namespace that the pod is running in. */
  public static final StringAttributeSetter K8S_NAMESPACE =
      StringAttributeSetter.create("k8s.namespace.name");
  /** The name of the pod. */
  public static final StringAttributeSetter K8S_POD = StringAttributeSetter.create("k8s.pod.name");
  /** The name of the deployment. */
  public static final StringAttributeSetter K8S_DEPLOYMENT =
      StringAttributeSetter.create("k8s.deployment.name");
  /** Hostname of the host. It contains what the `hostname` command returns on the host machine. */
  public static final StringAttributeSetter HOST_HOSTNAME =
      StringAttributeSetter.create("host.hostname");
  /** Unique host id. For Cloud this must be the instance_id assigned by the cloud provider. */
  public static final StringAttributeSetter HOST_ID = StringAttributeSetter.create("host.id");
  /**
   * Name of the host. It may contain what `hostname` returns on Unix systems, the fully qualified,
   * or a name specified by the user.
   */
  public static final StringAttributeSetter HOST_NAME = StringAttributeSetter.create("host.name");
  /** Type of host. For Cloud this must be the machine type. */
  public static final StringAttributeSetter HOST_TYPE = StringAttributeSetter.create("host.type");
  /** Name of the VM image or OS install the host was instantiated from. */
  public static final StringAttributeSetter HOST_IMAGE_NAME =
      StringAttributeSetter.create("host.image.name");
  /** VM image id. For Cloud, this value is from the provider. */
  public static final StringAttributeSetter HOST_IMAGE_ID =
      StringAttributeSetter.create("host.image.id");
  /** The version string of the VM image. */
  public static final StringAttributeSetter HOST_IMAGE_VERSION =
      StringAttributeSetter.create("host.image.version");
  /** Name of the cloud provider. */
  public static final StringAttributeSetter CLOUD_PROVIDER =
      StringAttributeSetter.create("cloud.provider");
  /** The cloud account id used to identify different entities. */
  public static final StringAttributeSetter CLOUD_ACCOUNT =
      StringAttributeSetter.create("cloud.account.id");
  /** A specific geographical location where different entities can run. */
  public static final StringAttributeSetter CLOUD_REGION =
      StringAttributeSetter.create("cloud.region");
  /** Zones are a sub set of the region connected through low-latency links. */
  public static final StringAttributeSetter CLOUD_ZONE = StringAttributeSetter.create("cloud.zone");

  private ResourceAttributes() {}
}
