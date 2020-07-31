/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opentracingshim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.DefaultSpan;
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
                OpenTelemetry.getTracer("opentracingshim"),
                OpenTelemetry.getCorrelationContextManager(),
                OpenTelemetry.getPropagators()));
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
    io.opentelemetry.trace.Span span = ((SpanShim) otSpan).getSpan();

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
    io.opentelemetry.trace.Span span = ((SpanShim) otSpan).getSpan();
    assertTrue(span instanceof DefaultSpan);
  }
}
