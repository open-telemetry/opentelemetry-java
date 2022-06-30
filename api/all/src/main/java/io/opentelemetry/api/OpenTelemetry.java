/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;

/**
 * The entrypoint to telemetry functionality for tracing, metrics and baggage.
 *
 * <p>If using the OpenTelemetry SDK, you may want to instantiate the {@link OpenTelemetry} to
 * provide configuration, for example of {@code Resource} or {@code Sampler}. See {@code
 * OpenTelemetrySdk} and {@code OpenTelemetrySdk.builder} for information on how to construct the
 * SDK {@link OpenTelemetry}.
 *
 * @see TracerProvider
 * @see ContextPropagators
 */
public interface OpenTelemetry {
  /** Returns a completely no-op {@link OpenTelemetry}. */
  static OpenTelemetry noop() {
    return DefaultOpenTelemetry.getNoop();
  }

  /**
   * Returns an {@link OpenTelemetry} which will do remote propagation of {@link
   * io.opentelemetry.context.Context} using the provided {@link ContextPropagators} and is no-op
   * otherwise.
   */
  static OpenTelemetry propagating(ContextPropagators propagators) {
    return DefaultOpenTelemetry.getPropagating(propagators);
  }

  /** Returns the {@link TracerProvider} for this {@link OpenTelemetry}. */
  TracerProvider getTracerProvider();

  /**
   * Gets or creates a named tracer instance from the {@link TracerProvider} for this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationScopeName) {
    return getTracerProvider().get(instrumentationScopeName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the {@link TracerProvider} in this
   * {@link OpenTelemetry}.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @param instrumentationScopeVersion The version of the instrumentation scope (e.g., "1.0.0").
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationScopeName, String instrumentationScopeVersion) {
    return getTracerProvider().get(instrumentationScopeName, instrumentationScopeVersion);
  }

  /**
   * Creates a {@link TracerBuilder} for a named {@link Tracer} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a TracerBuilder instance.
   * @since 1.4.0
   */
  default TracerBuilder tracerBuilder(String instrumentationScopeName) {
    return getTracerProvider().tracerBuilder(instrumentationScopeName);
  }

  /**
   * Returns the {@link MeterProvider} for this {@link OpenTelemetry}.
   *
   * @since 1.10.0
   */
  default MeterProvider getMeterProvider() {
    return MeterProvider.noop();
  }

  /**
   * Gets or creates a named meter instance from the {@link MeterProvider} for this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Meter instance.
   * @since 1.10.0
   */
  default Meter getMeter(String instrumentationScopeName) {
    return getMeterProvider().get(instrumentationScopeName);
  }

  /**
   * Creates a {@link MeterBuilder} for a named {@link Tracer} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a MeterBuilder instance.
   * @since 1.10.0
   */
  default MeterBuilder meterBuilder(String instrumentationScopeName) {
    return getMeterProvider().meterBuilder(instrumentationScopeName);
  }

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();
}
