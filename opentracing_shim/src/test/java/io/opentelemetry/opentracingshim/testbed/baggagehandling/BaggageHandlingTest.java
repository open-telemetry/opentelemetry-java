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

package io.opentelemetry.opentracingshim.testbed.baggagehandling;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.junit.Test;

public final class BaggageHandlingTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);

  @Test
  public void test() throws Exception {
    Span span = tracer.buildSpan("one").start();
    try (Scope scope = tracer.activateSpan(span)) {
      SpanContext ctx1 = span.context();

      span.setBaggageItem("key1", "value1");
      assertEquals(true, span.context().baggageItems().iterator().hasNext());
      assertEquals(span.getBaggageItem("key1"), "value1");
      assertEquals(tracer.activeSpan().getBaggageItem("key1"), "value1");

      // The original SpanContext was not modified.
      assertFalse(ctx1.baggageItems().iterator().hasNext());

      tracer.activeSpan().setBaggageItem("key2", "value2");
      assertEquals(span.getBaggageItem("key2"), "value2");
      assertEquals(tracer.activeSpan().getBaggageItem("key2"), "value2");
    }
  }
}
