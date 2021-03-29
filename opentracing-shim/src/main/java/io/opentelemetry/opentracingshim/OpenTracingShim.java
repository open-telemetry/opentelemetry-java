/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;

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
    return new TracerShim(new TelemetryInfo(tracer, OpenTracingPropagators.builder().build()));
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim using the provided OpenTelemetry instance.
   *
   * @param openTelemetry the {@code OpenTelemetry} instance used to create this shim.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim(OpenTelemetry openTelemetry) {
    return new TracerShim(
        new TelemetryInfo(
            getTracer(openTelemetry.getTracerProvider()),
            OpenTracingPropagators.builder()
                .setTextMap(openTelemetry.getPropagators().getTextMapPropagator())
                .setHttpHeaders(openTelemetry.getPropagators().getTextMapPropagator())
                .build()));
  }

  private static Tracer getTracer(TracerProvider tracerProvider) {
    return tracerProvider.get("opentracingshim");
  }
}
