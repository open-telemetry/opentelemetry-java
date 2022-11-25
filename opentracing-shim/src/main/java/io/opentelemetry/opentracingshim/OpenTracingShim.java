/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * Factory for creating an OpenTracing {@link io.opentracing.Tracer} that is implemented using the
 * OpenTelemetry APIs.
 */
public final class OpenTracingShim {
  private OpenTracingShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of {@code
   * GlobalOpenTelemetry.getTracerProvider()} and {@code GlobalOpenTelemetry.getPropagators()}.
   *
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim() {
    return createTracerShim(getTracer(GlobalOpenTelemetry.getTracerProvider()));
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using provided Tracer instance and {@code
   * GlobalOpenTelemetry.getPropagators()}.
   *
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim(Tracer tracer) {
    return createTracerShim(tracer, OpenTracingPropagators.builder().build());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using provided Tracer instance and {@code
   * OpenTracingPropagators} instance.
   *
   * @return a {@code io.opentracing.Tracer}.
   * @since 1.1.0
   */
  public static io.opentracing.Tracer createTracerShim(
      Tracer tracer, OpenTracingPropagators propagators) {
    return new TracerShim(new TelemetryInfo(tracer, propagators));
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using the provided OpenTelemetry instance.
   *
   * @param openTelemetry the {@code OpenTelemetry} instance used to create this shim.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim(OpenTelemetry openTelemetry) {
    return createTracerShim(
        getTracer(openTelemetry.getTracerProvider()),
        OpenTracingPropagators.builder()
            .setTextMap(openTelemetry.getPropagators().getTextMapPropagator())
            .setHttpHeaders(openTelemetry.getPropagators().getTextMapPropagator())
            .build());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using the provided {@code TracerProvider} and
   * {@code TextMapPropagator} instance.
   *
   * @param provider the {@code TracerProvider} instance used to create this shim.
   * @param propagator the {@code TextMapPropagator} instance used to create this shim.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim(
      TracerProvider provider, TextMapPropagator propagator) {
    return createTracerShim(
        getTracer(provider),
        OpenTracingPropagators.builder().setTextMap(propagator).setHttpHeaders(propagator).build());
  }

  private static Tracer getTracer(TracerProvider tracerProvider) {
    return tracerProvider.get("opentracing-shim");
  }
}
