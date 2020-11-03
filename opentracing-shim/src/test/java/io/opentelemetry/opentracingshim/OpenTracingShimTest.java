/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class OpenTracingShimTest {

  @Test
  void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) OpenTracingShim.createTracerShim();
    assertEquals(OpenTelemetry.getGlobalTracer("opentracingshim"), tracerShim.tracer());
  }

  @Test
  void createTracerShim_fromOpenTelemetryInstance() {
    OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    when(openTelemetry.getTracerProvider()).thenReturn(sdk);
    when(openTelemetry.getPropagators()).thenReturn(mock(ContextPropagators.class));

    TracerShim tracerShim = (TracerShim) OpenTracingShim.createTracerShim(openTelemetry);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
  }
}
