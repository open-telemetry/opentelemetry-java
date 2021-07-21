/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class ExecutorServiceSpanProcessorFlushBenchmark {

  private static class DelayingSpanExporter implements SpanExporter {

    private final ScheduledExecutorService executor;

    private final int delayMs;

    private DelayingSpanExporter(int delayMs) {
      executor = Executors.newScheduledThreadPool(5);
      this.delayMs = delayMs;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      final CompletableResultCode result = new CompletableResultCode();
      executor.schedule((Runnable) result::succeed, delayMs, TimeUnit.MILLISECONDS);
      return result;
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }

  @Param({"0", "1", "5"})
  private int delayMs;

  @Param({"1000", "2000", "5000"})
  private int spanCount;

  private List<Span> spans;

  private ExecutorServiceSpanProcessor processor;

  @Setup(Level.Trial)
  public final void setup() {
    SpanExporter exporter = new DelayingSpanExporter(delayMs);
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    processor = ExecutorServiceSpanProcessor.builder(exporter, executor, true).build();
    ImmutableList.Builder<Span> spans = ImmutableList.builderWithExpectedSize(spanCount);
    Tracer tracer = SdkTracerProvider.builder().build().get("benchmarkTracer");
    for (int i = 0; i < spanCount; i++) {
      spans.add(tracer.spanBuilder("span").startSpan());
    }
    this.spans = spans.build();
  }

  @TearDown(Level.Trial)
  public final void tearDown() {
    processor.shutdown().join(10, TimeUnit.SECONDS);
  }

  /** Export spans through {@link ExecutorServiceSpanProcessor}. */
  @Benchmark
  @Fork(1)
  @Threads(5)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void export() {
    for (Span span : spans) {
      processor.onEnd((ReadableSpan) span);
    }
    processor.forceFlush().join(10, TimeUnit.MINUTES);
  }
}
