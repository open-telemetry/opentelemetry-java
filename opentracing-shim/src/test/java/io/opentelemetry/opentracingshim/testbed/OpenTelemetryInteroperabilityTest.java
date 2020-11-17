/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryInteroperabilityTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final io.opentelemetry.api.trace.Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer("opentracingshim");

  private final Tracer otTracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());

  @Test
  void sdkContinuesOpenTracingTrace() {
    Span otSpan = otTracer.buildSpan("ot_span").start();
    try (Scope scope = otTracer.scopeManager().activate(otSpan)) {
      tracer.spanBuilder("otel_span").startSpan().end();
    } finally {
      otSpan.finish();
    }
    assertThat(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid()).isFalse();
    assertThat(otTracer.activeSpan()).isNull();

    List<SpanData> finishedSpans = otelTesting.getSpans();
    assertThat(finishedSpans).hasSize(2);
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
    assertThat(otTracer.activeSpan()).isNull();

    List<SpanData> finishedSpans = otelTesting.getSpans();
    assertThat(finishedSpans).hasSize(2);
    TestUtils.assertSameTrace(finishedSpans);
  }
}
