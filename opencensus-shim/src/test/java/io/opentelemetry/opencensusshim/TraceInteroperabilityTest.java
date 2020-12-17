/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceInteroperabilityTest {

  private static final String NULL_SPAN_ID = "0000000000000000";

  @Captor private ArgumentCaptor<Collection<SpanData>> spanDataCaptor;

  @Spy private SpanExporter spanExporter;

  @BeforeEach
  public void init() {
    when(spanExporter.export(any())).thenReturn(CompletableResultCode.ofSuccess());
    SpanProcessor spanProcessor = SimpleSpanProcessor.builder(spanExporter).build();
    OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(spanProcessor);
  }

  @Test
  void testParentChildRelationshipsAreExportedCorrectly() {
    Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.test.scoped.span.1");
    Span span = tracer.spanBuilder("OpenTelemetrySpan").startSpan();
    try (Scope scope = Context.current().with(span).makeCurrent()) {
      span.addEvent("OpenTelemetry: Event 1");
      createOpenCensusScopedSpanWithChildSpan(
          /* withInnerOpenTelemetrySpan= */ true, /* withInnerOpenCensusSpan= */ false);
      span.addEvent("OpenTelemetry: Event 2");
    } finally {
      span.end();
    }

    verify(spanExporter, times(3)).export(spanDataCaptor.capture());
    Collection<SpanData> export1 = spanDataCaptor.getAllValues().get(0);
    Collection<SpanData> export2 = spanDataCaptor.getAllValues().get(1);
    Collection<SpanData> export3 = spanDataCaptor.getAllValues().get(2);

    assertThat(export1.size()).isEqualTo(1);
    SpanData spanData1 = export1.iterator().next();
    assertThat(spanData1.getName()).isEqualTo("OpenTelemetrySpan2");
    assertThat(spanData1.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData1.getEvents().get(0).getName()).isEqualTo("OpenTelemetry2: Event 1");
    assertThat(spanData1.getEvents().get(1).getName()).isEqualTo("OpenTelemetry2: Event 2");

    assertThat(export2.size()).isEqualTo(1);
    SpanData spanData2 = export2.iterator().next();
    assertThat(spanData2.getName()).isEqualTo("OpenCensusSpan1");
    assertThat(spanData2.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData2.getEvents().get(0).getName()).isEqualTo("OpenCensus1: Event 1");
    assertThat(spanData2.getEvents().get(1).getName()).isEqualTo("OpenCensus1: Event 2");

    assertThat(export3.size()).isEqualTo(1);
    SpanData spanData3 = export3.iterator().next();
    assertThat(spanData3.getName()).isEqualTo("OpenTelemetrySpan");
    assertThat(spanData3.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData3.getEvents().get(0).getName()).isEqualTo("OpenTelemetry: Event 1");
    assertThat(spanData3.getEvents().get(1).getName()).isEqualTo("OpenTelemetry: Event 2");

    assertThat(spanData1.getParentSpanId()).isEqualTo(spanData2.getSpanId());
    assertThat(spanData2.getParentSpanId()).isEqualTo(spanData3.getSpanId());
    assertThat(spanData3.getParentSpanId()).isEqualTo(NULL_SPAN_ID);
  }

  @Test
  void testParentChildRelationshipsAreExportedCorrectlyForOpenCensusOnly() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilder("OpenCensusSpan")
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.addAnnotation("OpenCensus: Event 1");
      createOpenCensusScopedSpanWithChildSpan(
          /* withInnerOpenTelemetrySpan= */ false, /* withInnerOpenCensusSpan= */ true);
      span.addAnnotation("OpenCensus: Event 2");
    }
    Tracing.getExportComponent().shutdown();

    verify(spanExporter, times(3)).export(spanDataCaptor.capture());
    Collection<SpanData> export1 = spanDataCaptor.getAllValues().get(0);
    Collection<SpanData> export2 = spanDataCaptor.getAllValues().get(1);
    Collection<SpanData> export3 = spanDataCaptor.getAllValues().get(2);

    assertThat(export1.size()).isEqualTo(1);
    SpanData spanData1 = export1.iterator().next();
    assertThat(spanData1.getName()).isEqualTo("OpenCensusSpan2");
    assertThat(spanData1.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData1.getEvents().get(0).getName()).isEqualTo("OpenCensus2: Event 1");
    assertThat(spanData1.getEvents().get(1).getName()).isEqualTo("OpenCensus2: Event 2");

    assertThat(export2.size()).isEqualTo(1);
    SpanData spanData2 = export2.iterator().next();
    assertThat(spanData2.getName()).isEqualTo("OpenCensusSpan1");
    assertThat(spanData2.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData2.getEvents().get(0).getName()).isEqualTo("OpenCensus1: Event 1");
    assertThat(spanData2.getEvents().get(1).getName()).isEqualTo("OpenCensus1: Event 2");

    assertThat(export3.size()).isEqualTo(1);
    SpanData spanData3 = export3.iterator().next();
    assertThat(spanData3.getName()).isEqualTo("OpenCensusSpan");
    assertThat(spanData3.getTotalRecordedEvents()).isEqualTo(2);
    assertThat(spanData3.getEvents().get(0).getName()).isEqualTo("OpenCensus: Event 1");
    assertThat(spanData3.getEvents().get(1).getName()).isEqualTo("OpenCensus: Event 2");

    assertThat(spanData1.getParentSpanId()).isEqualTo(spanData2.getSpanId());
    assertThat(spanData2.getParentSpanId()).isEqualTo(spanData3.getSpanId());
    assertThat(spanData3.getParentSpanId()).isEqualTo(NULL_SPAN_ID);
  }

  private static void createOpenCensusScopedSpanWithChildSpan(
      boolean withInnerOpenTelemetrySpan, boolean withInnerOpenCensusSpan) {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilder("OpenCensusSpan1")
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.addAnnotation("OpenCensus1: Event 1");
      if (withInnerOpenTelemetrySpan) {
        createOpenTelemetryScopedSpan();
      }
      if (withInnerOpenCensusSpan) {
        createOpenCensusScopedSpan();
      }
      span.addAnnotation("OpenCensus1: Event 2");
    }
    Tracing.getExportComponent().shutdown();
  }

  private static void createOpenCensusScopedSpan() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilder("OpenCensusSpan2")
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.addAnnotation("OpenCensus2: Event 1");
      span.addAnnotation("OpenCensus2: Event 2");
    }
    Tracing.getExportComponent().shutdown();
  }

  private static void createOpenTelemetryScopedSpan() {
    Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.test.scoped.span.2");
    Span span = tracer.spanBuilder("OpenTelemetrySpan2").startSpan();
    try (Scope scope = Context.current().with(span).makeCurrent()) {
      span.addEvent("OpenTelemetry2: Event 1");
      span.addEvent("OpenTelemetry2: Event 2");
    } finally {
      span.end();
    }
  }
}
