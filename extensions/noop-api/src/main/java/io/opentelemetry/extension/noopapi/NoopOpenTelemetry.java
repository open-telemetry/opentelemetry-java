/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.noopapi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;

/**
 * An implementation of {@link OpenTelemetry} that is completely no-op. Unlike {@link
 * OpenTelemetry#noop()}, this implementation does not support in-process context propagation
 * atjavajava all. This means that no objects are allocated nor {@link ThreadLocal}s used in an
 * application using this implementation. This can be a good option for use in frameworks shared
 * across a large number of servers to introduce instrumentation without forcing overhead on any
 * users of the framework. If such overhead is not a concern, always use either {@link
 * OpenTelemetry#noop()}, {@link OpenTelemetry#propagating(ContextPropagators)}, or the
 * OpenTelemetry SDK.
 *
 * <p>The following code will fail because context is not mounted.
 *
 * <pre>{@code
 * try (Scope ignored = Context.current().with(Span.wrap(VALID_SPAN_CONTEXT).makeCurrent()) {
 *   assert Span.current().spanContext().equals(VALID_SPAN_CONTEXT);
 * }
 * }</pre>
 *
 * <p>In most cases when instrumenting a library, the above pattern does not happen because {@link
 * io.opentelemetry.api.trace.Span#wrap(SpanContext)} is primarily for use in remote propagators.
 * The common pattern looks like
 *
 * <pre>{@code
 * Span span = tracer.spanBuilder().setAttribute(...).startSpan();
 * try (Scope ignored = Context.current().with(span).makeCurrent()) {
 *   assert Span.current().spanContext().equals(SpanContext.getInvalid());
 * }
 * }</pre>
 *
 * <p>The above will succeed both with the {@linkplain OpenTelemetry#noop() default implementation}
 * and this one, but with this implementation there will be no overhead at all.
 */
public class NoopOpenTelemetry implements OpenTelemetry {

  private static final OpenTelemetry INSTANCE = new NoopOpenTelemetry();

  public static OpenTelemetry getInstance() {
    return INSTANCE;
  }

  @Override
  public TracerProvider getTracerProvider() {
    return NoopTracerProvider.INSTANCE;
  }

  @Override
  public MeterProvider getMeterProvider() {
    // Default implementation is already truly no-op.
    return MeterProvider.noop();
  }

  @Override
  public ContextPropagators getPropagators() {
    return ContextPropagators.noop();
  }

  private NoopOpenTelemetry() {}
}
