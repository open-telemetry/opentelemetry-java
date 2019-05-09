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
 * API for distributed tracing.
 *
 * <p>Distributed tracing, also called distributed request tracing, is a technique that helps
 * debugging distributed applications.
 *
 * <p>Trace represents a tree of spans. A trace has a root span that encapsulates all the spans from
 * start to end, and the children spans being the distinct calls invoked in between.
 *
 * <p>{@link io.opentelemetry.trace.Span} represents a single operation within a trace.
 *
 * <p>{@link io.opentelemetry.trace.Span Spans} are propagated in-process in the {@code
 * io.grpc.Context} and between process using one of the wire propagation formats supported in the
 * {@code opentelemetry.trace.propagation} package.
 */
// TODO: Add code examples.
package io.opentelemetry.trace;
