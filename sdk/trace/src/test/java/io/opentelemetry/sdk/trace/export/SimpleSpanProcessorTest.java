/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorTest.WaitingSpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for {@link SimpleSpanProcessor}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleSpanProcessorTest {
  private static final String SPAN_NAME = "MySpanName";
  @Mock private ReadableSpan readableSpan;
  @Mock private ReadWriteSpan readWriteSpan;
  @Mock private SpanExporter spanExporter;
  @Mock private Sampler mockSampler;
  private static final SpanContext SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getSampled(),
          TraceState.getDefault());
  private static final SpanContext NOT_SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault());

  private SpanProcessor simpleSampledSpansProcessor;

  @BeforeEach
  void setUp() {
    simpleSampledSpansProcessor = SimpleSpanProcessor.create(spanExporter);
    when(spanExporter.export(anyCollection())).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void createNull() {
    assertThatThrownBy(() -> SimpleSpanProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void onStartSync() {
    simpleSampledSpansProcessor.onStart(Context.root(), readWriteSpan);
    verifyNoInteractions(spanExporter);
  }

  @Test
  void onEndSync_SampledSpan() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(spanData));
  }

  @Test
  void onEndSync_NotSampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verifyNoInteractions(spanExporter);
  }

  @Test
  void onEndSync_ExportUnsampledSpans_NotSampledSpan() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    SpanProcessor simpleSpanProcessor =
        SimpleSpanProcessor.builder(spanExporter).setExportUnsampledSpans(true).build();
    simpleSpanProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(spanData));
  }

  @Test
  void onEndSync_ExportUnsampledSpans_SampledSpan() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    SpanProcessor simpleSpanProcessor =
        SimpleSpanProcessor.builder(spanExporter).setExportUnsampledSpans(true).build();
    simpleSpanProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(spanData));
  }

  @Test
  void tracerSdk_SampledSpan() {
    WaitingSpanExporter waitingSpanExporter =
        new WaitingSpanExporter(1, CompletableResultCode.ofSuccess());

    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(waitingSpanExporter))
            .setSampler(mockSampler)
            .build();

    when(mockSampler.shouldSample(any(), any(), any(), any(), any(), anyList()))
        .thenReturn(SamplingResult.drop());

    try {
      Tracer tracer = sdkTracerProvider.get(getClass().getName());
      tracer.spanBuilder(SPAN_NAME).startSpan();
      tracer.spanBuilder(SPAN_NAME).startSpan();

      when(mockSampler.shouldSample(any(), any(), any(), any(), any(), anyList()))
          .thenReturn(SamplingResult.recordAndSample());
      Span span = tracer.spanBuilder(SPAN_NAME).startSpan();
      span.end();

      // Spans are recorded and exported in the same order as they are ended, we test that a non
      // sampled span is not exported by creating and ending a sampled span after a non sampled span
      // and checking that the first exported span is the sampled span (the non sampled did not get
      // exported).
      List<SpanData> exported = waitingSpanExporter.waitForExport();
      // Need to check this because otherwise the variable span1 is unused, other option is to not
      // have a span1 variable.
      assertThat(exported).containsExactly(((ReadableSpan) span).toSpanData());
    } finally {
      sdkTracerProvider.shutdown();
    }
  }

  @Test
  void tracerSdk_ExportUnsampledSpans_NotSampledSpan() {
    WaitingSpanExporter waitingSpanExporter =
        new WaitingSpanExporter(1, CompletableResultCode.ofSuccess());

    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(
                SimpleSpanProcessor.builder(waitingSpanExporter)
                    .setExportUnsampledSpans(true)
                    .build())
            .setSampler(mockSampler)
            .build();

    when(mockSampler.shouldSample(any(), any(), any(), any(), any(), anyList()))
        .thenReturn(SamplingResult.drop());

    try {
      Tracer tracer = sdkTracerProvider.get(getClass().getName());
      tracer.spanBuilder(SPAN_NAME).startSpan();
      tracer.spanBuilder(SPAN_NAME).startSpan();

      when(mockSampler.shouldSample(any(), any(), any(), any(), any(), anyList()))
          .thenReturn(SamplingResult.recordOnly());
      Span span = tracer.spanBuilder(SPAN_NAME).startSpan();
      span.end();

      // Spans are recorded and exported in the same order as they are ended, we test that a non
      // sampled span is not exported by creating and ending a sampled span after a non sampled span
      // and checking that the first exported span is the sampled span (the non sampled did not get
      // exported).
      List<SpanData> exported = waitingSpanExporter.waitForExport();
      // Need to check this because otherwise the variable span1 is unused, other option is to not
      // have a span1 variable.
      assertThat(exported).containsExactly(((ReadableSpan) span).toSpanData());
    } finally {
      sdkTracerProvider.shutdown();
    }
  }

  @Test
  void onEndSync_ExporterReturnError() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    // Try again, now will no longer return error.
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter, times(2)).export(Collections.singletonList(spanData));
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(spanExporter.export(any())).thenReturn(export1, export2);

    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);

    simpleSampledSpansProcessor.onEnd(readableSpan);
    simpleSampledSpansProcessor.onEnd(readableSpan);

    verify(spanExporter, times(2)).export(Collections.singletonList(spanData));

    CompletableResultCode flush = simpleSampledSpansProcessor.forceFlush();
    assertThat(flush.isDone()).isFalse();

    export1.succeed();
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void shutdown() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(spanExporter.export(any())).thenReturn(export1, export2);

    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);

    simpleSampledSpansProcessor.onEnd(readableSpan);
    simpleSampledSpansProcessor.onEnd(readableSpan);

    verify(spanExporter, times(2)).export(Collections.singletonList(spanData));

    CompletableResultCode shutdown = simpleSampledSpansProcessor.shutdown();
    assertThat(shutdown.isDone()).isFalse();

    export1.succeed();
    assertThat(shutdown.isDone()).isFalse();
    verify(spanExporter, never()).shutdown();

    export2.succeed();
    assertThat(shutdown.isDone()).isTrue();
    assertThat(shutdown.isSuccess()).isTrue();
    verify(spanExporter).shutdown();
  }

  @Test
  void close() {
    simpleSampledSpansProcessor.close();
    verify(spanExporter).shutdown();
  }

  @Test
  void getSpanExporter() {
    assertThat(((SimpleSpanProcessor) SimpleSpanProcessor.create(spanExporter)).getSpanExporter())
        .isSameAs(spanExporter);
  }

  @Test
  @SuppressWarnings("unchecked")
  void verifyMetricsDisabledByDefault() {
    Supplier<MeterProvider> mockSupplier = Mockito.mock(Supplier.class);

    when(spanExporter.export(any())).thenReturn(CompletableResultCode.ofSuccess());

    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);

    SimpleSpanProcessor processor =
        SimpleSpanProcessor.builder(spanExporter).setMeterProvider(mockSupplier).build();

    processor.onEnd(readableSpan);

    verifyNoInteractions(mockSupplier);
  }

  @Test
  void verifySemConvMetrics() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();

    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build()) {

      SpanData spanData = TestUtils.makeBasicSpan();
      when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
      when(readableSpan.toSpanData()).thenReturn(spanData);

      SimpleSpanProcessor processor =
          SimpleSpanProcessor.builder(spanExporter)
              .setMeterProvider(() -> meterProvider)
              .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
              .build();

      CompletableResultCode blockedResultCode = new CompletableResultCode();
      when(spanExporter.export(any())).thenReturn(blockedResultCode);

      processor.onEnd(readableSpan);

      assertThat(metricReader.collectAllMetrics())
          .hasSize(1)
          .anySatisfy(
              metric ->
                  OpenTelemetryAssertions.assertThat(metric)
                      .hasName("otel.sdk.processor.span.processed")
                      .hasLongSumSatisfying(
                          size ->
                              size.hasPointsSatisfying(
                                  point ->
                                      point
                                          .hasValue(1)
                                          .hasAttributesSatisfying(
                                              attribs -> {
                                                assertThat(attribs.size()).isEqualTo(2);
                                                assertThat(
                                                        attribs.get(
                                                            SemConvAttributes.OTEL_COMPONENT_TYPE))
                                                    .isEqualTo("simple_span_processor");
                                                String componentName =
                                                    attribs.get(
                                                        SemConvAttributes.OTEL_COMPONENT_NAME);
                                                assertThat(componentName)
                                                    .matches("simple_span_processor/\\d+");
                                              }))));

      // Stop blocking to allow a fast termination of the test case
      blockedResultCode.succeed();
    }
  }
}
