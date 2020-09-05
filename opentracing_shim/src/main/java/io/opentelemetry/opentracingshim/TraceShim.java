/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import java.util.Objects;

public final class TraceShim {
  private TraceShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of {@code OpenTelemetry.getTracer()} and
   * {@code OpenTelemetry.getCorrelationContextManager()}.
   *
   * @return a {@code io.opentracing.Tracer}.
   * @since 0.1.0
   */
  public static io.opentracing.Tracer createTracerShim() {
    return new TracerShim(
        new TelemetryInfo(
            getTracer(OpenTelemetry.getTracerProvider()),
            OpenTelemetry.getCorrelationContextManager(),
            OpenTelemetry.getPropagators()));
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out the specified {@code Tracer} and {@code
   * CorrelationContextManager}.
   *
   * @param tracerProvider the {@code TracerProvider} used by this shim.
   * @param contextManager the {@code CorrelationContextManager} used by this shim.
   * @return a {@code io.opentracing.Tracer}.
   * @since 0.1.0
   */
  public static io.opentracing.Tracer createTracerShim(
      TracerProvider tracerProvider, CorrelationContextManager contextManager) {
    return new TracerShim(
        new TelemetryInfo(
            getTracer(Objects.requireNonNull(tracerProvider, "tracerProvider")),
            Objects.requireNonNull(contextManager, "contextManager"),
            OpenTelemetry.getPropagators()));
  }

  private static Tracer getTracer(TracerProvider tracerProvider) {
    return tracerProvider.get("opentracingshim");
  }
}
