/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.trace.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdk}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
class TracerSdkTest {

  private static final String SPAN_NAME = "span_name";
  private static final String INSTRUMENTATION_LIBRARY_NAME =
      "io.opentelemetry.sdk.trace.TracerSdkTest";
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "semver:0.2.0";
  private static final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create(
          INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);
  @Mock private Span span;
  private final TracerSdk tracer =
      TracerSdkProvider.builder()
          .build()
          .get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void defaultGetCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void defaultSpanBuilder() {
    assertThat(tracer.spanBuilder(SPAN_NAME)).isInstanceOf(SpanBuilderSdk.class);
  }

  @Test
  void getCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Context origContext = TracingContextUtils.withSpan(span, Context.current()).attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void withSpan_NullSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    try (Scope ignored = tracer.withSpan(null)) {
      assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void getCurrentSpan_WithSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    try (Scope ignored = tracer.withSpan(span)) {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
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
    TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
    tracerSdkProvider.addSpanProcessor(spanProcessor);
    TracerSdk tracer =
        tracerSdkProvider.get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

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
    SpanProcessor spanProcessor = BatchSpanProcessor.newBuilder(countingSpanExporter).build();
    TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
    tracerSdkProvider.addSpanProcessor(spanProcessor);
    TracerSdk tracer =
        tracerSdkProvider.get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setTracer(tracer).setSpanProcessor(spanProcessor);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(2_000, 1, new SimpleSpanOperation(tracer)));
    }

    stressTestBuilder.build().run();
    assertThat(countingSpanExporter.numberOfSpansExported.get()).isEqualTo(8_000);
  }

  private static class CountingSpanProcessor implements SpanProcessor {
    private final AtomicLong numberOfSpansStarted = new AtomicLong();
    private final AtomicLong numberOfSpansFinished = new AtomicLong();

    @Override
    public void onStart(ReadableSpan span) {
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

    @Override
    public void shutdown() {
      // no-op
    }

    @Override
    public void forceFlush() {
      // no-op
    }
  }

  private static class SimpleSpanOperation implements OperationUpdater {
    private final TracerSdk tracer;

    public SimpleSpanOperation(TracerSdk tracer) {
      this.tracer = tracer;
    }

    @Override
    public void update() {
      Span span = tracer.spanBuilder("testSpan").startSpan();
      try (Scope ignored = tracer.withSpan(span)) {
        span.setAttribute("testAttribute", AttributeValue.stringAttributeValue("testValue"));
      } finally {
        span.end();
      }
    }
  }

  private static class CountingSpanExporter implements SpanExporter {

    public AtomicLong numberOfSpansExported = new AtomicLong();

    @Override
    public ResultCode export(Collection<SpanData> spans) {
      numberOfSpansExported.addAndGet(spans.size());
      return ResultCode.SUCCESS;
    }

    @Override
    public ResultCode flush() {
      return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {
      // no-op
    }
  }
}
