/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class SpanBuilderShimTest {

  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, OpenTelemetry.getGlobalPropagators());

  private static final String SPAN_NAME = "Span";

  @Test
  void baggage_parent() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertEquals(childSpan.getBaggageItem("key1"), "value1");
        assertEquals(
            getBaggageMap(childSpan.context().baggageItems()),
            getBaggageMap(parentSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_parentContext() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan.context()).start();
      try {
        assertEquals(childSpan.getBaggageItem("key1"), "value1");
        assertEquals(
            getBaggageMap(childSpan.context().baggageItems()),
            getBaggageMap(parentSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void parent_NullContextShim() {
    /* SpanContextShim is null until Span.context() or Span.getBaggageItem() are called.
     * Verify a null SpanContextShim in the parent is handled properly. */
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertFalse(childSpan.context().baggageItems().iterator().hasNext());
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }
}
