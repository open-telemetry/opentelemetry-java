/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * API for resource information population.
 *
 * <p>The resources library primarily defines a type "Resource" that captures information about the
 * entity for which stats or traces are recorded. For example, metrics exposed by a Kubernetes
 * container can be linked to a resource that specifies the cluster, namespace, pod, and container
 * name.
 *
 * <p>One environment variables is used to populate resource information:
 *
 * <ul>
 *   <li>OTEL_RESOURCE_ATTRIBUTES: A comma-separated list of attributes describing the source in
 *       more detail, e.g. “key1=val1,key2=val2”. The allowed character set is appropriately
 *       constrained.
 * </ul>
 *
 * <p>Attribute keys, and attribute values MUST contain only printable ASCII (codes between 32 and
 * 126, inclusive) and less than 256 characters. Type and attribute keys MUST have a length greater
 * than zero. They SHOULD start with a domain name and separate hierarchies with / characters, e.g.
 * k8s.io/namespace/name.
 *
 * <p>One environment variable is used to disable resource provider implementations that are found
 * on the classpath:
 *
 * <ul>
 *   <li>OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS: A comma-separated list of fully qualified class
 *       names representing resource provider implementations that are found on the classpath but
 *       should be disabled.
 * </ul>
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.resources;

import javax.annotation.ParametersAreNonnullByDefault;
