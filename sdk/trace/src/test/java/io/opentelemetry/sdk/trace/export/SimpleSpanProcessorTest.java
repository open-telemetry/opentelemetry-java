/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorTest.WaitingSpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for {@link SimpleSpanProcessor}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleSpanProcessorTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final int CONCURRENT_ITERATIONS = 100;
  private static final int CONCURRENT_EXPORTS = 16;
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

    when(mockSampler.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void createNull() {
    assertThatThrownBy(() -> SimpleSpanProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  @SuppressLogger(SimpleSpanProcessor.class)
  void onEndSync_ExporterThrowsCheckedException() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    when(spanExporter.export(anyCollection()))
        .thenAnswer(invocation -> {
          throw new Exception("No export for you.");
        });

    assertThatCode(() -> simpleSampledSpansProcessor.onEnd(readableSpan))
        .doesNotThrowAnyException();
    verify(spanExporter).export(Collections.singletonList(spanData));
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
  void forceFlush_NoPendingExports() {
    CompletableResultCode flush = simpleSampledSpansProcessor.forceFlush();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_FailedExport() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(spanExporter.export(any())).thenReturn(export1, export2);

    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(TestUtils.makeBasicSpan());

    simpleSampledSpansProcessor.onEnd(readableSpan);
    simpleSampledSpansProcessor.onEnd(readableSpan);

    CompletableResultCode flush = simpleSampledSpansProcessor.forceFlush();
    RuntimeException error = new RuntimeException("export failed");
    export1.failExceptionally(error);
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isFalse();
    assertThat(flush.getFailureThrowable()).isSameAs(error);
  }

  @Test
  void forceFlush_RepeatedAfterExportsComplete() {
    CompletableResultCode export = new CompletableResultCode();

    when(spanExporter.export(any())).thenReturn(export);

    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(TestUtils.makeBasicSpan());

    simpleSampledSpansProcessor.onEnd(readableSpan);

    CompletableResultCode flush1 = simpleSampledSpansProcessor.forceFlush();
    export.succeed();
    assertThat(flush1.isDone()).isTrue();
    assertThat(flush1.isSuccess()).isTrue();

    for (int i = 0; i < 3; i++) {
      CompletableResultCode flush = simpleSampledSpansProcessor.forceFlush();
      assertThat(flush.isDone()).isTrue();
      assertThat(flush.isSuccess()).isTrue();
    }
    assertThat(flush1.isSuccess()).isTrue();
  }

  // Regression test for https://github.com/open-telemetry/opentelemetry-java/issues/8995: exports
  // completing while forceFlush() collects them must not leave the flush result incomplete. The
  // exact interleaving cannot be forced without production test hooks, so race a bounded number of
  // times. Once every export has completed, a correct flush result is expected to be done already,
  // so the check does not depend on a timeout.
  @Test
  void forceFlush_ExportsCompleteConcurrently() throws InterruptedException {
    for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
      List<CompletableResultCode> exports =
          endAsyncExports(simpleSampledSpansProcessor, CONCURRENT_EXPORTS);

      CompletableResultCode flush =
          callWhileCompleting(exports, simpleSampledSpansProcessor::forceFlush);

      assertThat(exports).allMatch(CompletableResultCode::isDone);
      assertThat(flush.isDone()).as("flush done in iteration %s", i).isTrue();
      assertThat(flush.isSuccess()).isTrue();
    }
  }

  @Test
  void shutdown_ExportsCompleteConcurrently() throws InterruptedException {
    for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
      SpanProcessor processor = SimpleSpanProcessor.create(spanExporter);
      List<CompletableResultCode> exports = endAsyncExports(processor, CONCURRENT_EXPORTS);

      CompletableResultCode shutdown = callWhileCompleting(exports, processor::shutdown);

      assertThat(exports).allMatch(CompletableResultCode::isDone);
      assertThat(shutdown.isDone()).as("shutdown done in iteration %s", i).isTrue();
      assertThat(shutdown.isSuccess()).isTrue();
    }
    verify(spanExporter, times(CONCURRENT_ITERATIONS)).shutdown();
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

  @SuppressWarnings("MockitoDoSetup")
  private List<CompletableResultCode> endAsyncExports(SpanProcessor processor, int count) {
    List<CompletableResultCode> exports = new ArrayList<>();
    // doAnswer avoids invoking the previous answer while re-stubbing, which would record an extra
    // result that was never exported.
    doAnswer(
            invocation -> {
              CompletableResultCode export = new CompletableResultCode();
              exports.add(export);
              return export;
            })
        .when(spanExporter)
        .export(any());
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(TestUtils.makeBasicSpan());
    for (int i = 0; i < count; i++) {
      processor.onEnd(readableSpan);
    }
    return exports;
  }

  /** Invokes {@code call} while another thread completes {@code exports}. */
  private static CompletableResultCode callWhileCompleting(
      List<CompletableResultCode> exports, Supplier<CompletableResultCode> call)
      throws InterruptedException {
    CountDownLatch ready = new CountDownLatch(1);
    CountDownLatch start = new CountDownLatch(1);
    Thread completer =
        new Thread(
            () -> {
              ready.countDown();
              try {
                if (!start.await(10, TimeUnit.SECONDS)) {
                  return;
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }
              exports.forEach(CompletableResultCode::succeed);
            });
    // Daemon, and every wait below is bounded, so a regression fails the test instead of hanging
    // the build. The bounds are failure guards; on a passing run each wait is normally very short.
    completer.setDaemon(true);
    completer.start();
    try {
      assertThat(ready.await(10, TimeUnit.SECONDS)).as("completer thread started").isTrue();
      start.countDown();
      return call.get();
    } finally {
      start.countDown();
      completer.join(TimeUnit.SECONDS.toMillis(10));
      assertThat(completer.isAlive()).as("completer thread finished").isFalse();
    }
  }
}
