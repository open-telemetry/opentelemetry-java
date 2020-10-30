/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import java.util.Objects;

public final class TraceShim {
  private TraceShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of {@code
   * OpenTelemetry.getGlobalTracerProvider()} and {@code OpenTelemetry.getGlobalPropagators()}.
   *
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim() {
    return new TracerShim(
        new TelemetryInfo(
            getTracer(OpenTelemetry.getGlobalTracerProvider()),
            OpenTelemetry.getGlobalPropagators()));
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out the specified {@code Tracer}. This uses
   * ContextPropagators from the global {@link OpenTelemetry} instance.
   *
   * @param tracerProvider the {@code TracerProvider} used by this shim.
   * @return a {@code io.opentracing.Tracer}.
   */
  public static io.opentracing.Tracer createTracerShim(TracerProvider tracerProvider) {
    return new TracerShim(
        new TelemetryInfo(
            getTracer(Objects.requireNonNull(tracerProvider, "tracerProvider")),
            OpenTelemetry.getGlobalPropagators()));
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
            getTracer(openTelemetry.getTracerProvider()), openTelemetry.getPropagators()));
  }

  private static Tracer getTracer(TracerProvider tracerProvider) {
    return tracerProvider.get("opentracingshim");
  }
}
