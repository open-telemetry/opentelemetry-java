/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class TraceShimTest {

  @Test
  void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim();
    assertEquals(OpenTelemetry.getTracer("opentracingshim"), tracerShim.tracer());
  }

  @Test
  void createTracerShim_nullTracer() {
    assertThrows(
        NullPointerException.class, () -> TraceShim.createTracerShim(null), "tracerProvider");
  }

  @Test
  void createTracerShim() {
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(sdk);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
  }
}
