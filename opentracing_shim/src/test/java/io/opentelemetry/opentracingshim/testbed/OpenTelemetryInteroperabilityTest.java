/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetryInteroperabilityTest {
  private final io.opentelemetry.api.trace.Tracer tracer =
      OpenTelemetry.getGlobalTracer("opentracingshim");
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder()
          .setTracerSdkManagement(OpenTelemetrySdk.getGlobalTracerManagement())
          .build();
  private final Tracer otTracer =
      TraceShim.createTracerShim(OpenTelemetry.getGlobalTracerProvider());

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
    assertThat(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid()).isFalse();
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }

  @Test
  void openTracingContinuesSdkTrace() {
    io.opentelemetry.api.trace.Span otelSpan = tracer.spanBuilder("otel_span").startSpan();
    try (io.opentelemetry.context.Scope scope = otelSpan.makeCurrent()) {
      otTracer.buildSpan("ot_span").start().finish();
    } finally {
      otelSpan.end();
    }

    assertThat(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid()).isFalse();
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }
}
