/*
 * Copyright 2019, OpenTelemetry Authors
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
 */
package io.opentelemetry.sdk.resources;
