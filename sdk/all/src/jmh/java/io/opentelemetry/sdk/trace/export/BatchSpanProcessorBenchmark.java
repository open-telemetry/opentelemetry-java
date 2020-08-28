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

package io.opentelemetry.sdk.trace.export;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class BatchSpanProcessorBenchmark {

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
      executor.schedule(
          new Runnable() {
            @Override
            public void run() {
              result.succeed();
            }
          },
          delayMs,
          TimeUnit.MILLISECONDS);
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

  private BatchSpanProcessor processor;

  @Setup(Level.Trial)
  public final void setup() {
    SpanExporter exporter = new DelayingSpanExporter(delayMs);
    processor = BatchSpanProcessor.newBuilder(exporter).build();

    ImmutableList.Builder<Span> spans = ImmutableList.builderWithExpectedSize(spanCount);
    Tracer tracer = OpenTelemetry.getTracerProvider().get("benchmarkTracer");
    for (int i = 0; i < spanCount; i++) {
      spans.add(tracer.spanBuilder("span").startSpan());
    }
    this.spans = spans.build();
  }

  /** Export spans through {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}. */
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
