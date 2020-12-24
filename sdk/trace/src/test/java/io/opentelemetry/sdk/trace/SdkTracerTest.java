/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.trace.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link SdkTracer}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
@ExtendWith(MockitoExtension.class)
class SdkTracerTest {

  private static final String SPAN_NAME = "span_name";
  private static final String INSTRUMENTATION_LIBRARY_NAME =
      "io.opentelemetry.sdk.trace.TracerSdkTest";
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "0.2.0";
  private static final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create(
          INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);
  @Mock private Span span;
  private final SdkTracer tracer =
      (SdkTracer)
          SdkTracerProvider.builder()
              .build()
              .get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

  @Test
  void defaultSpanBuilder() {
    assertThat(tracer.spanBuilder(SPAN_NAME)).isInstanceOf(SdkSpanBuilder.class);
  }

  @Test
  void getInstrumentationLibraryInfo() {
    assertThat(tracer.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
  }

  @Test
  void propagatesInstrumentationLibraryInfoToSpan() {
    ReadableSpan readableSpan = (ReadableSpan) tracer.spanBuilder("spanName").startSpan();
    assertThat(readableSpan.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
  }

  @Test
  void stressTest() {
    CountingSpanProcessor spanProcessor = new CountingSpanProcessor();
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();
    SdkTracer tracer =
        (SdkTracer)
            sdkTracerProvider.get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setTracer(tracer).setSpanProcessor(spanProcessor);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(2_000, 1, new SimpleSpanOperation(tracer)));
    }

    stressTestBuilder.build().run();
    assertThat(spanProcessor.numberOfSpansFinished.get()).isEqualTo(8_000);
    assertThat(spanProcessor.numberOfSpansStarted.get()).isEqualTo(8_000);
  }

  @Test
  void stressTest_withBatchSpanProcessor() {
    CountingSpanExporter countingSpanExporter = new CountingSpanExporter();
    SpanProcessor spanProcessor = BatchSpanProcessor.builder(countingSpanExporter).build();
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();
    SdkTracer tracer =
        (SdkTracer)
            sdkTracerProvider.get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setTracer(tracer).setSpanProcessor(spanProcessor);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(2_000, 1, new SimpleSpanOperation(tracer)));
    }

    // Needs to correlate with the BatchSpanProcessor.Builder's default, which is the only thing
    // this test can guarantee
    final int defaultMaxQueueSize = 2048;

    stressTestBuilder.build().run();
    assertThat(countingSpanExporter.numberOfSpansExported.get())
        .isGreaterThanOrEqualTo(defaultMaxQueueSize);
  }

  private static class CountingSpanProcessor implements SpanProcessor {
    private final AtomicLong numberOfSpansStarted = new AtomicLong();
    private final AtomicLong numberOfSpansFinished = new AtomicLong();

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
      numberOfSpansStarted.incrementAndGet();
    }

    @Override
    public boolean isStartRequired() {
      return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
      numberOfSpansFinished.incrementAndGet();
    }

    @Override
    public boolean isEndRequired() {
      return true;
    }
  }

  private static class SimpleSpanOperation implements OperationUpdater {
    private final SdkTracer tracer;

    public SimpleSpanOperation(SdkTracer tracer) {
      this.tracer = tracer;
    }

    @Override
    public void update() {
      Span span = tracer.spanBuilder("testSpan").startSpan();
      try (Scope ignored = span.makeCurrent()) {
        span.setAttribute("testAttribute", "testValue");
      } finally {
        span.end();
      }
    }
  }

  private static class CountingSpanExporter implements SpanExporter {

    public final AtomicLong numberOfSpansExported = new AtomicLong();

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      numberOfSpansExported.addAndGet(spans.size());
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      // no-op
      return CompletableResultCode.ofSuccess();
    }
  }
}
