/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class InteroperabilityTest {

  private static final String NULL_SPAN_ID = "0000000000000000";

  @Captor private ArgumentCaptor<Collection<SpanData>> spanDataCaptor;

  @Spy private SpanExporter spanExporter;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    when(spanExporter.export(any())).thenReturn(CompletableResultCode.ofSuccess());
    SpanProcessor spanProcessor = SimpleSpanProcessor.builder(spanExporter).build();
    OpenTelemetrySdk.getTracerManagement().addSpanProcessor(spanProcessor);
  }

  @Test
  public void testParentChildRelationshipsAreExportedCorrectly() {
    Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.test.scoped.span.1");
    Span span = tracer.spanBuilder("OpenTelemetrySpan").startSpan();
    try (Scope scope = TracingContextUtils.currentContextWith(span)) {
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

    assertEquals(1, export1.size());
    SpanData spanData1 = export1.iterator().next();
    assertEquals("OpenTelemetrySpan2", spanData1.getName());
    assertEquals(2, spanData1.getTotalRecordedEvents());
    assertEquals("OpenTelemetry2: Event 1", spanData1.getEvents().get(0).getName());
    assertEquals("OpenTelemetry2: Event 2", spanData1.getEvents().get(1).getName());

    assertEquals(1, export2.size());
    SpanData spanData2 = export2.iterator().next();
    assertEquals("OpenCensusSpan1", spanData2.getName());
    assertEquals(2, spanData2.getTotalRecordedEvents());
    assertEquals("OpenCensus1: Event 1", spanData2.getEvents().get(0).getName());
    assertEquals("OpenCensus1: Event 2", spanData2.getEvents().get(1).getName());

    assertEquals(1, export3.size());
    SpanData spanData3 = export3.iterator().next();
    assertEquals("OpenTelemetrySpan", spanData3.getName());
    assertEquals(2, spanData3.getTotalRecordedEvents());
    assertEquals("OpenTelemetry: Event 1", spanData3.getEvents().get(0).getName());
    assertEquals("OpenTelemetry: Event 2", spanData3.getEvents().get(1).getName());

    assertEquals(spanData2.getSpanId(), spanData1.getParentSpanId());
    assertEquals(spanData3.getSpanId(), spanData2.getParentSpanId());
    assertEquals(NULL_SPAN_ID, spanData3.getParentSpanId());
  }

  @Test
  public void testParentChildRelationshipsAreExportedCorrectlyForOpenCensusOnly() {
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

    assertEquals(1, export1.size());
    SpanData spanData1 = export1.iterator().next();
    assertEquals("OpenCensusSpan2", spanData1.getName());
    assertEquals(2, spanData1.getTotalRecordedEvents());
    assertEquals("OpenCensus2: Event 1", spanData1.getEvents().get(0).getName());
    assertEquals("OpenCensus2: Event 2", spanData1.getEvents().get(1).getName());

    assertEquals(1, export2.size());
    SpanData spanData2 = export2.iterator().next();
    assertEquals("OpenCensusSpan1", spanData2.getName());
    assertEquals(2, spanData2.getTotalRecordedEvents());
    assertEquals("OpenCensus1: Event 1", spanData2.getEvents().get(0).getName());
    assertEquals("OpenCensus1: Event 2", spanData2.getEvents().get(1).getName());

    assertEquals(1, export3.size());
    SpanData spanData3 = export3.iterator().next();
    assertEquals("OpenCensusSpan", spanData3.getName());
    assertEquals(2, spanData3.getTotalRecordedEvents());
    assertEquals("OpenCensus: Event 1", spanData3.getEvents().get(0).getName());
    assertEquals("OpenCensus: Event 2", spanData3.getEvents().get(1).getName());

    assertEquals(spanData2.getSpanId(), spanData1.getParentSpanId());
    assertEquals(spanData3.getSpanId(), spanData2.getParentSpanId());
    assertEquals(NULL_SPAN_ID, spanData3.getParentSpanId());
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
    Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.test.scoped.span.2");
    Span span = tracer.spanBuilder("OpenTelemetrySpan2").startSpan();
    try (Scope scope = TracingContextUtils.currentContextWith(span)) {
      span.addEvent("OpenTelemetry2: Event 1");
      span.addEvent("OpenTelemetry2: Event 2");
    } finally {
      span.end();
    }
  }
}
