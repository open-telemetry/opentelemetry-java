/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TracerShimTest {

  TracerShim tracerShim;

  @BeforeEach
  void setUp() {
    tracerShim =
        new TracerShim(
            new TelemetryInfo(
                OpenTelemetry.getGlobalTracer("opentracingshim"),
                OpenTelemetry.getGlobalPropagators()));
  }

  @Test
  void defaultTracer() {
    assertNotNull(tracerShim.buildSpan("one"));
    assertNotNull(tracerShim.scopeManager());
    assertNull(tracerShim.activeSpan());
    assertNull(tracerShim.scopeManager().activeSpan());
  }

  @Test
  void activateSpan() {
    Span otSpan = tracerShim.buildSpan("one").start();
    io.opentelemetry.api.trace.Span span = ((SpanShim) otSpan).getSpan();

    assertNull(tracerShim.activeSpan());
    assertNull(tracerShim.scopeManager().activeSpan());

    try (Scope scope = tracerShim.activateSpan(otSpan)) {
      assertNotNull(tracerShim.activeSpan());
      assertNotNull(tracerShim.scopeManager().activeSpan());
      assertEquals(span, ((SpanShim) tracerShim.activeSpan()).getSpan());
      assertEquals(span, ((SpanShim) tracerShim.scopeManager().activeSpan()).getSpan());
    }

    assertNull(tracerShim.activeSpan());
    assertNull(tracerShim.scopeManager().activeSpan());
  }

  @Test
  void extract_nullContext() {
    SpanContext result =
        tracerShim.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(Collections.emptyMap()));
    assertNull(result);
  }

  @Test
  void inject_nullContext() {
    Map<String, String> map = new HashMap<>();
    tracerShim.inject(null, Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    assertEquals(0, map.size());
  }

  @Test
  void close() {
    tracerShim.close();
    Span otSpan = tracerShim.buildSpan(null).start();
    io.opentelemetry.api.trace.Span span = ((SpanShim) otSpan).getSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
  }
}
