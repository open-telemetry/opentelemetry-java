/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Tracer is the interface for {@link Span} creation and interaction with the in-process context.
 *
 * <p>Users may choose to use manual or automatic Context propagation. Because of that this class
 * offers APIs to facilitate both usages.
 *
 * <p>The automatic context propagation is done using {@link io.opentelemetry.context.Context} which
 * is a gRPC independent implementation for in-process Context propagation mechanism which can carry
 * scoped-values across API boundaries and between threads. Users of the library must propagate the
 * {@link io.opentelemetry.context.Context} between different threads.
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = OpenTelemetry.getTracer();
 *   void doWork() {
 *     Span span = tracer.spanBuilder("MyClass.DoWork").startSpan();
 *     try (Scope ignored = span.makeCurrent()) {
 *       Span.current().addEvent("Starting the work.");
 *       doWorkInternal();
 *       Span.current().addEvent("Finished working.");
 *     } finally {
 *       span.end();
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with manual context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = OpenTelemetry.getTracer();
 *   void doWork(Span parent) {
 *     Span childSpan = tracer.spanBuilder("MyChildSpan")
 *         setParent(parent).startSpan();
 *     childSpan.addEvent("Starting the work.");
 *     try {
 *       doSomeWork(childSpan); // Manually propagate the new span down the stack.
 *     } finally {
 *       // To make sure we end the span even in case of an exception.
 *       childSpan.end();  // Manually end the span.
 *     }
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface Tracer {

  /**
   * Returns a {@link SpanBuilder} to create and start a new {@link Span}.
   *
   * <p>See {@link SpanBuilder} for usage examples.
   *
   * @param spanName The name of the returned Span.
   * @return a {@code Span.Builder} to create and start a new {@code Span}.
   * @throws NullPointerException if {@code spanName} is {@code null}.
   */
  SpanBuilder spanBuilder(String spanName);
}
