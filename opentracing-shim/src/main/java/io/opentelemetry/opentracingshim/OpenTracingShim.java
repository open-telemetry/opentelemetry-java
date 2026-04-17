/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentracing.Tracer;

/**
 * Factory for creating an OpenTracing {@link io.opentracing.Tracer} that is implemented using the
 * OpenTelemetry APIs.
 *
 * @since 1.26.0
 */
public final class OpenTracingShim {
  private OpenTracingShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim using the provided {@link OpenTelemetry} instance.
   * Uses the {@link TracerProvider} and {@link TextMapPropagator} associated with the {@link
   * OpenTelemetry} instance.
   *
   * @param openTelemetry the {@code OpenTelemetry} instance used to create this shim.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static Tracer createTracerShim(OpenTelemetry openTelemetry) {
    TextMapPropagator propagator = openTelemetry.getPropagators().getTextMapPropagator();
    return createTracerShim(openTelemetry.getTracerProvider(), propagator, propagator);
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using the provided {@code TracerProvider} and
   * {@code TextMapPropagator} instance.
   *
   * @param provider the {@code TracerProvider} instance used to create this shim.
   * @param textMapPropagator the propagator used for {@link
   *     io.opentracing.propagation.Format.Builtin#TEXT_MAP} format.
   * @param httpPropagator the propagator used for {@link
   *     io.opentracing.propagation.Format.Builtin#HTTP_HEADERS} format.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static Tracer createTracerShim(
      TracerProvider provider,
      TextMapPropagator textMapPropagator,
      TextMapPropagator httpPropagator) {
    return new TracerShim(provider, textMapPropagator, httpPropagator);
  }
}
