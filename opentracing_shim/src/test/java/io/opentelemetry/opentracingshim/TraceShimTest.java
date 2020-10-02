/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.OpenTelemetry.getBaggageManager;
import static io.opentelemetry.OpenTelemetry.getTracerProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class TraceShimTest {

  @Test
  void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim();
    assertEquals(OpenTelemetry.getTracer("opentracingshim"), tracerShim.tracer());
    assertEquals(OpenTelemetry.getBaggageManager(), tracerShim.contextManager());
  }

  @Test
  void createTracerShim_nullTracer() {
    assertThrows(
        NullPointerException.class,
        () -> TraceShim.createTracerShim(null, getBaggageManager()),
        "tracerProvider");
  }

  @Test
  void createTracerShim_nullContextManager() {
    assertThrows(
        NullPointerException.class,
        () -> TraceShim.createTracerShim(getTracerProvider(), null),
        "contextManager");
  }

  @Test
  void createTracerShim() {
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    BaggageManagerSdk contextManager = new BaggageManagerSdk();
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(sdk, contextManager);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
    assertEquals(contextManager, tracerShim.contextManager());
  }
}
