/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetryInteroperabilityTest {
  private final io.opentelemetry.trace.Tracer tracer = OpenTelemetry.getTracer("opentracingshim");
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder()
          .setTracerSdkManagement(OpenTelemetrySdk.getTracerManagement())
          .build();
  private final Tracer otTracer =
      TraceShim.createTracerShim(
          OpenTelemetry.getTracerProvider(), DefaultBaggageManager.getInstance());

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
