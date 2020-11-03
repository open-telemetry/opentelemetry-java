/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanShimTest {

  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, OpenTelemetry.getGlobalPropagators());
  private Span span;

  private static final String SPAN_NAME = "Span";

  @BeforeEach
  void setUp() {
    span = telemetryInfo.tracer().spanBuilder(SPAN_NAME).startSpan();
  }

  @AfterEach
  void tearDown() {
    span.end();
  }

  @Test
  void context_simple() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertNotNull(contextShim);
    assertEquals(contextShim.getSpanContext(), span.getSpanContext());
    assertEquals(contextShim.toTraceId(), span.getSpanContext().getTraceIdAsHexString().toString());
    assertEquals(contextShim.toSpanId(), span.getSpanContext().getSpanIdAsHexString().toString());
    assertFalse(contextShim.baggageItems().iterator().hasNext());
  }

  @Test
  void baggage() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

    spanShim.setBaggageItem("key1", "value1");
    spanShim.setBaggageItem("key2", "value2");
    assertEquals(spanShim.getBaggageItem("key1"), "value1");
    assertEquals(spanShim.getBaggageItem("key2"), "value2");

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertNotNull(contextShim);
    Map<String, String> baggageMap = getBaggageMap(contextShim.baggageItems());
    assertEquals(2, baggageMap.size());
    assertEquals(baggageMap.get("key1"), "value1");
    assertEquals(baggageMap.get("key2"), "value2");
  }

  @Test
  void baggage_replacement() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    SpanContextShim contextShim1 = (SpanContextShim) spanShim.context();

    spanShim.setBaggageItem("key1", "value1");
    SpanContextShim contextShim2 = (SpanContextShim) spanShim.context();
    assertNotEquals(contextShim1, contextShim2);
    assertFalse(contextShim1.baggageItems().iterator().hasNext()); /* original, empty */
    assertTrue(contextShim2.baggageItems().iterator().hasNext()); /* updated, with values */
  }

  @Test
  void baggage_differentShimObjs() {
    SpanShim spanShim1 = new SpanShim(telemetryInfo, span);
    spanShim1.setBaggageItem("key1", "value1");

    /* Baggage should be synchronized among different SpanShim objects
     * referring to the same Span.*/
    SpanShim spanShim2 = new SpanShim(telemetryInfo, span);
    spanShim2.setBaggageItem("key1", "value2");
    assertEquals(spanShim1.getBaggageItem("key1"), "value2");
    assertEquals(spanShim2.getBaggageItem("key1"), "value2");
    assertEquals(
        getBaggageMap(spanShim1.context().baggageItems()),
        getBaggageMap(spanShim2.context().baggageItems()));
  }
}
