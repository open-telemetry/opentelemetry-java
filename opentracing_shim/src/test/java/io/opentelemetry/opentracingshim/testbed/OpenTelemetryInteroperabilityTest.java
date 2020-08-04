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

package io.opentelemetry.opentracingshim.testbed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetryInteroperabilityTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final io.opentelemetry.trace.Tracer tracer = sdk.get("opentracingshim");
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerProvider(sdk).build();
  private final Tracer otTracer =
      TraceShim.createTracerShim(sdk, DefaultCorrelationContextManager.getInstance());

  @BeforeEach
  void before() {
    inMemoryTracing.getSpanExporter().reset();
  }

  @Test
  void sdkContinuesOpenTracingTrace() {
    Span otSpan = otTracer.buildSpan("ot_span").start();
    try (Scope scope = otTracer.scopeManager().activate(otSpan)) {
      tracer.spanBuilder("otel_span").startSpan().end();
    } finally {
      otSpan.finish();
    }
    assertEquals(tracer.getCurrentSpan().getClass(), DefaultSpan.class);
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }

  @Test
  void openTracingContinuesSdkTrace() {
    io.opentelemetry.trace.Span otelSpan = tracer.spanBuilder("otel_span").startSpan();
    try (io.opentelemetry.context.Scope scope = tracer.withSpan(otelSpan)) {
      otTracer.buildSpan("ot_span").start().finish();
    } finally {
      otelSpan.end();
    }

    assertEquals(tracer.getCurrentSpan().getClass(), DefaultSpan.class);
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }
}
