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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class SdkTracerTest {

  private static final String SPAN_NAME = "span_name";
  private static final String INSTRUMENTATION_SCOPE_NAME =
      "io.opentelemetry.sdk.trace.TracerSdkTest";
  private static final String INSTRUMENTATION_SCOPE_VERSION = "0.2.0";
  private static final InstrumentationScopeInfo instrumentationScopeInfo =
      InstrumentationScopeInfo.builder(INSTRUMENTATION_SCOPE_NAME)
          .setVersion(INSTRUMENTATION_SCOPE_VERSION)
          .setSchemaUrl("http://schemaurl")
          .build();
  private final SdkTracer tracer =
      (SdkTracer)
          SdkTracerProvider.builder()
              .build()
              .tracerBuilder(INSTRUMENTATION_SCOPE_NAME)
              .setInstrumentationVersion(INSTRUMENTATION_SCOPE_VERSION)
              .setSchemaUrl("http://schemaurl")
              .build();

  @Test
  void defaultSpanBuilder() {
    assertThat(tracer.spanBuilder(SPAN_NAME)).isInstanceOf(SdkSpanBuilder.class);
  }

  @Test
  void getInstrumentationScopeInfo() {
    assertThat(tracer.getInstrumentationScopeInfo()).isEqualTo(instrumentationScopeInfo);
  }

  @Test
  void updateEnabled() {
    tracer.updateTracerConfig(TracerConfig.disabled());
    assertThat(tracer.isEnabled()).isFalse();
    tracer.updateTracerConfig(TracerConfig.enabled());
    assertThat(tracer.isEnabled()).isTrue();
  }

  @Test
  void propagatesInstrumentationScopeInfoToSpan() {
    ReadableSpan readableSpan = (ReadableSpan) tracer.spanBuilder("spanName").startSpan();
    assertThat(readableSpan.getInstrumentationScopeInfo()).isEqualTo(instrumentationScopeInfo);
  }

  @Test
  void fallbackSpanName() {
    ReadableSpan readableSpan = (ReadableSpan) tracer.spanBuilder("  ").startSpan();
    assertThat(readableSpan.getName()).isEqualTo(SdkTracer.FALLBACK_SPAN_NAME);

    readableSpan = (ReadableSpan) tracer.spanBuilder(null).startSpan();
    assertThat(readableSpan.getName()).isEqualTo(SdkTracer.FALLBACK_SPAN_NAME);
  }

  @Test
  void stressTest() {
    CountingSpanProcessor spanProcessor = new CountingSpanProcessor();
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();
    SdkTracer tracer =
        (SdkTracer)
            sdkTracerProvider.get(INSTRUMENTATION_SCOPE_NAME, INSTRUMENTATION_SCOPE_VERSION);

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
            sdkTracerProvider.get(INSTRUMENTATION_SCOPE_NAME, INSTRUMENTATION_SCOPE_VERSION);

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setTracer(tracer).setSpanProcessor(spanProcessor);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(2_000, 1, new SimpleSpanOperation(tracer)));
    }

    // Needs to correlate with the BatchSpanProcessor.Builder's default, which is the only thing
    // this test can guarantee
    int defaultMaxQueueSize = 2048;

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

    private SimpleSpanOperation(SdkTracer tracer) {
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

    private final AtomicLong numberOfSpansExported = new AtomicLong();

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
