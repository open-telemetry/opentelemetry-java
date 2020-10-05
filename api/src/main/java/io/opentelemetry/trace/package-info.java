/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
