/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TraceInteroperabilityTest {

  private static final String NULL_SPAN_ID = "0000000000000000";

  // Initialize OpenTelemetry statically because OpenCensus is.
  private static final SpanExporter spanExporter;
  private static final OpenTelemetry openTelemetry;

  static {
    spanExporter = spy(SpanExporter.class);
    when(spanExporter.export(anyList())).thenReturn(CompletableResultCode.ofSuccess());

    SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);
    openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
            .build();
    GlobalOpenTelemetry.set(openTelemetry);
  }

  @Captor private ArgumentCaptor<Collection<SpanData>> spanDataCaptor;

  @BeforeEach
  void resetMocks() {
    reset(spanExporter);
  }

  @Test
  void testParentChildRelationshipsAreExportedCorrectly() {
    Tracer tracer = openTelemetry.getTracer("io.opentelemetry.test.scoped.span.1");
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
  void testRemoteParent() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    io.opencensus.trace.Span remoteParentSpan =
        tracer.spanBuilder("remote parent span").startSpan();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilderWithRemoteParent("OpenCensusSpan", remoteParentSpan.getContext())
            .setSpanKind(Kind.SERVER)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startScopedSpan()) {
      remoteParentSpan.addAnnotation("test");
    }
    Tracing.getExportComponent().shutdown();

    verify(spanExporter, times(1)).export(spanDataCaptor.capture());
    Collection<SpanData> export1 = spanDataCaptor.getAllValues().get(0);

    assertThat(export1.size()).isEqualTo(1);
    SpanData spanData1 = export1.iterator().next();
    assertThat(spanData1.getName()).isEqualTo("OpenCensusSpan");
    assertThat(spanData1.getLinks().get(0).getSpanContext().getSpanIdAsHexString())
        .isEqualTo(remoteParentSpan.getContext().getSpanId().toLowerBase16());
  }

  @Test
  void testParentChildRelationshipsAreExportedCorrectlyForOpenCensusOnly() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    io.opencensus.trace.Span parentLinkSpan = tracer.spanBuilder("parent link span").startSpan();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilder("OpenCensusSpan")
            .setSpanKind(Kind.SERVER)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .setParentLinks(ImmutableList.of(parentLinkSpan))
            .startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.putAttributes(
          ImmutableMap.of(
              "testKey",
              AttributeValue.doubleAttributeValue(2.5),
              "testKey2",
              AttributeValue.booleanAttributeValue(false),
              "testKey3",
              AttributeValue.longAttributeValue(3)));
      span.addMessageEvent(
          MessageEvent.builder(MessageEvent.Type.SENT, 12345)
              .setUncompressedMessageSize(10)
              .setCompressedMessageSize(8)
              .build());
      span.addAnnotation(
          "OpenCensus: Event 1",
          ImmutableMap.of(
              "testKey",
              AttributeValue.doubleAttributeValue(123),
              "testKey2",
              AttributeValue.booleanAttributeValue(true)));
      span.addLink(Link.fromSpanContext(SpanContext.INVALID, Link.Type.PARENT_LINKED_SPAN));
      createOpenCensusScopedSpanWithChildSpan(
          /* withInnerOpenTelemetrySpan= */ false, /* withInnerOpenCensusSpan= */ true);
      span.addAnnotation(Annotation.fromDescription("OpenCensus: Event 2"));
      span.setStatus(Status.OK);
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
    assertThat(spanData3.getLinks().get(0).getSpanContext().getSpanIdAsHexString())
        .isEqualTo(parentLinkSpan.getContext().getSpanId().toLowerBase16());
    assertThat(spanData3.getKind()).isEqualTo(Span.Kind.SERVER);
    assertThat(spanData3.getStatus()).isEqualTo(SpanData.Status.ok());
    assertThat(spanData3.getAttributes().get(AttributeKey.doubleKey("testKey"))).isEqualTo(2.5);
    assertThat(spanData3.getAttributes().get(AttributeKey.booleanKey("testKey2"))).isEqualTo(false);
    assertThat(spanData3.getAttributes().get(AttributeKey.longKey("testKey3"))).isEqualTo(3);
    assertThat(spanData3.getTotalRecordedEvents()).isEqualTo(3);
    assertThat(spanData3.getEvents().get(0).getName()).isEqualTo("12345");
    assertThat(
            spanData3
                .getEvents()
                .get(0)
                .getAttributes()
                .get(AttributeKey.longKey("message.event.size.compressed")))
        .isEqualTo(8);
    assertThat(
            spanData3
                .getEvents()
                .get(0)
                .getAttributes()
                .get(AttributeKey.longKey("message.event.size.uncompressed")))
        .isEqualTo(10);
    assertThat(
            spanData3
                .getEvents()
                .get(0)
                .getAttributes()
                .get(AttributeKey.stringKey("message.event.type")))
        .isEqualTo("SENT");
    assertThat(spanData3.getEvents().get(1).getName()).isEqualTo("OpenCensus: Event 1");
    assertThat(spanData3.getEvents().get(1).getAttributes().get(AttributeKey.doubleKey("testKey")))
        .isEqualTo(123);
    assertThat(
            spanData3.getEvents().get(1).getAttributes().get(AttributeKey.booleanKey("testKey2")))
        .isEqualTo(true);
    assertThat(spanData3.getEvents().get(2).getName()).isEqualTo("OpenCensus: Event 2");

    assertThat(spanData1.getParentSpanId()).isEqualTo(spanData2.getSpanId());
    assertThat(spanData2.getParentSpanId()).isEqualTo(spanData3.getSpanId());
    assertThat(spanData3.getParentSpanId()).isEqualTo(NULL_SPAN_ID);
  }

  @Test
  void testOpenTelemetryMethodsOnOpenCensusSpans() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer
            .spanBuilder("OpenCensusSpan")
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startScopedSpan()) {
      OpenTelemetrySpanImpl span = (OpenTelemetrySpanImpl) tracer.getCurrentSpan();
      span.setStatus(StatusCode.ERROR);
      span.setAttribute("testKey", "testValue");
      span.addEvent("OpenCensus span: Event 1");
      span.addEvent(
          "OpenCensus span: Event 2",
          Attributes.of(AttributeKey.doubleKey("key2"), 3.5),
          0,
          TimeUnit.HOURS);
      span.updateName("OpenCensus Span renamed");
      span.addEvent("OpenCensus span: Event 3", 5, TimeUnit.MILLISECONDS);
      span.updateName("OpenCensus Span renamed");
      span.addEvent("OpenCensus span: Event 4", Attributes.of(AttributeKey.longKey("key3"), 4L));
      span.updateName("OpenCensus Span renamed");
    }
    Tracing.getExportComponent().shutdown();

    verify(spanExporter, times(1)).export(spanDataCaptor.capture());
    Collection<SpanData> export1 = spanDataCaptor.getAllValues().get(0);

    assertThat(export1.size()).isEqualTo(1);
    SpanData spanData1 = export1.iterator().next();
    assertThat(spanData1.getName()).isEqualTo("OpenCensus Span renamed");
    assertThat(spanData1.getTotalRecordedEvents()).isEqualTo(4);
    assertThat(spanData1.getEvents().get(0).getName()).isEqualTo("OpenCensus span: Event 1");
    assertThat(spanData1.getEvents().get(1).getName()).isEqualTo("OpenCensus span: Event 2");
    assertThat(spanData1.getEvents().get(1).getAttributes().get(AttributeKey.doubleKey("key2")))
        .isEqualTo(3.5);
    assertThat(spanData1.getEvents().get(1).getEpochNanos()).isEqualTo(0);
    assertThat(spanData1.getEvents().get(2).getName()).isEqualTo("OpenCensus span: Event 3");
    assertThat(spanData1.getEvents().get(2).getEpochNanos()).isEqualTo((long) 5e6);
    assertThat(spanData1.getEvents().get(3).getName()).isEqualTo("OpenCensus span: Event 4");
    assertThat(spanData1.getEvents().get(3).getAttributes().get(AttributeKey.longKey("key3")))
        .isEqualTo(4L);
    assertThat(spanData1.getAttributes().get(AttributeKey.stringKey("testKey")))
        .isEqualTo("testValue");
  }

  @Test
  public void testNoSampleDoesNotExport() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer.spanBuilder("OpenCensusSpan").setSampler(Samplers.neverSample()).startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.addAnnotation("OpenCensus: Event 1");
      span.addAnnotation("OpenCensus: Event 2");
      span.addAnnotation(Annotation.fromDescription("OpenCensus: Event 2"));
      span.setStatus(Status.RESOURCE_EXHAUSTED);
      span.putAttribute("testKey", AttributeValue.stringAttributeValue("testValue"));
    }
    Tracing.getExportComponent().shutdown();
    verify(spanExporter, never()).export(anyCollection());
  }

  @Test
  public void testNoRecordDoesNotExport() {
    io.opencensus.trace.Tracer tracer = Tracing.getTracer();
    try (io.opencensus.common.Scope scope =
        tracer.spanBuilder("OpenCensusSpan").setRecordEvents(false).startScopedSpan()) {
      io.opencensus.trace.Span span = tracer.getCurrentSpan();
      span.addAnnotation("OpenCensus: Event 1");
      span.addAnnotation(Annotation.fromDescription("OpenCensus: Event 2"));
      span.setStatus(Status.RESOURCE_EXHAUSTED);
      span.putAttribute("testKey", AttributeValue.stringAttributeValue("testValue"));
    }
    Tracing.getExportComponent().shutdown();
    verify(spanExporter, never()).export(anyCollection());
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
  }

  private static void createOpenTelemetryScopedSpan() {
    Tracer tracer = openTelemetry.getTracer("io.opentelemetry.test.scoped.span.2");
    Span span = tracer.spanBuilder("OpenTelemetrySpan2").startSpan();
    try (Scope scope = Context.current().with(span).makeCurrent()) {
      span.addEvent("OpenTelemetry2: Event 1");
      span.addEvent("OpenTelemetry2: Event 2");
    } finally {
      span.end();
    }
  }
}
