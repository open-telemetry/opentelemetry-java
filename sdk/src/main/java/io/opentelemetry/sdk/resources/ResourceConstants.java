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

package io.opentelemetry.sdk.resources;

/**
 * Provides constants for resource semantic conventions defined by the OpenTelemetry specification.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/data-resource-semantic-conventions.md">Resource
 *     Conventions</a>
 */
public class ResourceConstants {

  /**
   * Logical name of the service. MUST be the same for all instances of horizontally scaled
   * services.
   */
  public static final String SERVICE_NAME = "service.name";
  /**
   * A namespace for `service.name`. A string value having a meaning that helps to distinguish a
   * group of services,
   */
  public static final String SERVICE_NAMESPACE = "service.namespace";
  /**
   * The string ID of the service instance. MUST be unique for each instance of the same
   * `service.namespace,service.name` pair.
   */
  public static final String SERVICE_INSTANCE = "service.instance.id";
  /** The version string of the service API or implementation. */
  public static final String SERVICE_VERSION = "service.version";
  /** The name of the telemetry library. */
  public static final String LIBRARY_NAME = "library.name";
  /** The language of telemetry library and of the code instrumented with it. */
  public static final String LIBRARY_LANGUAGE = "library.language";
  /** The version string of the library. */
  public static final String LIBRARY_VERSION = "library.version";
  /** Container name. */
  public static final String CONTAINER_NAME = "container.name";
  /** Name of the image the container was built on. */
  public static final String CONTAINER_IMAGE_NAME = "container.image.name";
  /** Container image tag. */
  public static final String CONTAINER_IMAGE_TAG = "container.image.tag";
  /** The name of the cluster that the pod is running in. */
  public static final String K8S_CLUSTER = "k8s.cluster.name";
  /** The name of the namespace that the pod is running in. */
  public static final String K8S_NAMESPACE = "k8s.namespace.name";
  /** The name of the pod. */
  public static final String K8S_POD = "k8s.pod.name";
  /** The name of the deployment. */
  public static final String K8S_DEPLOYMENT = "k8s.deployment.name";
  /** Hostname of the host. It contains what the `hostname` command returns on the host machine. */
  public static final String HOST_HOSTNAME = "host.hostname";
  /** Unique host id. For Cloud this must be the instance_id assigned by the cloud provider. */
  public static final String HOST_ID = "host.id";
  /**
   * Name of the host. It may contain what `hostname` returns on Unix systems, the fully qualified,
   * or a name specified by the user.
   */
  public static final String HOST_NAME = "host.name";
  /** Type of host. For Cloud this must be the machine type. */
  public static final String HOST_TYPE = "host.type";
  /** Name of the VM image or OS install the host was instantiated from. */
  public static final String HOST_IMAGE_NAME = "host.image.name";
  /** VM image id. For Cloud, this value is from the provider. */
  public static final String HOST_IMAGE_ID = "host.image.id";
  /** The version string of the VM image. */
  public static final String HOST_IMAGE_VERSION = "host.image.version";
  /** Name of the cloud provider. */
  public static final String CLOUD_PROVIDER = "cloud.provider";
  /** The cloud account id used to identify different entities. */
  public static final String CLOUD_ACCOUNT = "cloud.account.id";
  /** A specific geographical location where different entities can run. */
  public static final String CLOUD_REGION = "cloud.region";
  /** Zones are a sub set of the region connected through low-latency links. */
  public static final String CLOUD_ZONE = "cloud.zone";

  private ResourceConstants() {}
}
