/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class TraceShimTest {

  @Test
  void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim();
    assertEquals(OpenTelemetry.getGlobalTracer("opentracingshim"), tracerShim.tracer());
  }

  @Test
  void createTracerShim_nullTracer() {
    assertThrows(
        NullPointerException.class,
        () -> TraceShim.createTracerShim((TracerProvider) null),
        "tracerProvider");
  }

  @Test
  void createTracerShim() {
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(sdk);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
  }

  @Test
  void createTracerShim_fromOpenTelemetryInstance() {
    OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    when(openTelemetry.getTracerProvider()).thenReturn(sdk);
    when(openTelemetry.getPropagators()).thenReturn(mock(ContextPropagators.class));

    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(openTelemetry);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
  }
}
