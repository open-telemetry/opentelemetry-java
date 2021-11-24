/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

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

  /** Returns the {@link MeterProvider} for this {@link OpenTelemetry}. */
  default MeterProvider getMeterProvider() {
    return MeterProvider.noop();
  }

  /**
   * Gets or creates a named tracer instance from the {@link TracerProvider} for this {@link
   * OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName) {
    return getTracerProvider().get(instrumentationName);
  }

  /**
   * Gets or creates a named and versioned tracer instance from the {@link TracerProvider} in this
   * {@link OpenTelemetry}.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g., "1.0.0").
   * @return a tracer instance.
   */
  default Tracer getTracer(String instrumentationName, String instrumentationVersion) {
    return getTracerProvider().get(instrumentationName, instrumentationVersion);
  }

  /**
   * Creates a {@link TracerBuilder} for a named {@link Tracer} instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a TracerBuilder instance.
   * @since 1.4.0
   */
  default TracerBuilder tracerBuilder(String instrumentationName) {
    return getTracerProvider().tracerBuilder(instrumentationName);
  }

  /** Returns the {@link ContextPropagators} for this {@link OpenTelemetry}. */
  ContextPropagators getPropagators();
}
