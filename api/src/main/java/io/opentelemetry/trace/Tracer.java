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

package io.opentelemetry.trace;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Tracer is the interface for {@link Span} creation and interaction with the in-process context.
 *
 * <p>Users may choose to use manual or automatic Context propagation. Because of that this class
 * offers APIs to facilitate both usages.
 *
 * <p>The automatic context propagation is done using {@link io.opentelemetry.context.Context} which
 * can carry scoped-values across API boundaries and between threads. Users of the library must
 * propagate the {@link io.opentelemetry.context.Context} between different threads.
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = OpenTelemetry.getTracer();
 *   void doWork() {
 *     Span span = tracer.spanBuilder("MyClass.DoWork").startSpan();
 *     try(Scope ss = tracer.withSpan(span)) {
 *       tracer.getCurrentSpan().addEvent("Starting the work.");
 *       doWorkInternal();
 *       tracer.getCurrentSpan().addEvent("Finished working.");
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
 *
 * @since 0.1.0
 */
// TODO (trask) update class javadoc
@ThreadSafe
public interface Tracer {
  /**
   * Returns a {@link Span.Builder} to create and start a new {@link Span}.
   *
   * <p>See {@link Span.Builder} for usage examples.
   *
   * @param spanName The name of the returned Span.
   * @return a {@code Span.Builder} to create and start a new {@code Span}.
   * @throws NullPointerException if {@code spanName} is {@code null}.
   * @since 0.1.0
   */
  Span.Builder spanBuilder(String spanName);
}
