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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

// @AuxCounters(AuxCounters.Type.EVENTS)
public class BatchSpanProcessorBenchmark {

  private static class DelayingSpanExporter implements SpanExporter {
    @SuppressWarnings("FutureReturnValueIgnored")
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public void shutdown() {}
  }

  //  private List<Span> spans;

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private final MetricProducer metricProducer =
        OpenTelemetrySdk.getMeterProvider().getMetricProducer();
    private BatchSpanProcessor processor;
    private Tracer tracer;
    private Collection<MetricData> allMetrics;

    @Setup(Level.Trial)
    public final void setup() {
      SpanExporter exporter = new DelayingSpanExporter();
      processor = BatchSpanProcessor.newBuilder(exporter).build();

      tracer = OpenTelemetry.getTracerProvider().get("benchmarkTracer");
    }

    @TearDown(Level.Trial)
    public final void tearDown() {
      processor.shutdown();
    }

    @TearDown(Level.Iteration)
    public final void recordMetrics() {
      allMetrics = metricProducer.collectAllMetrics();
      //      System.out.println(allMetrics);
    }
  }

  @State(Scope.Thread)
  @AuxCounters(AuxCounters.Type.EVENTS)
  public static class ThreadState {
    private Collection<MetricData> allMetrics;

    @TearDown(Level.Iteration)
    public final void recordMetrics(BenchmarkState benchmarkState) {
      allMetrics = benchmarkState.allMetrics;
    }

    /** Burn, checkstyle, burn. */
    public double dropRatio() {
      long exported = getMetric("exportedSpans");
      long dropped = getMetric("droppedSpans");
      long total = exported + dropped;
      if (total == 0) {
        return 0;
      } else {
        return (double) dropped / total / 5;
      }
    }

    public long exportedSpans() {
      return getMetric("exportedSpans");
    }

    public long droppedSpans() {
      return getMetric("droppedSpans");
    }

    private long getMetric(String metricName) {
      for (MetricData metricData : allMetrics) {
        if (metricData.getDescriptor().getName().equals(metricName)) {
          List<Point> points = new ArrayList<>(metricData.getPoints());
          if (points.isEmpty()) {
            return 0;
          } else {
            LongPoint point = (LongPoint) points.get(points.size() - 1);
            return point.getValue();
          }
        }
      }
      return 0;
    }
  }

  /** Export spans through {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}. */
  @Benchmark
  @Fork(1)
  @Threads(5)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 20)
  @BenchmarkMode(Mode.Throughput)
  public void export(BenchmarkState benchmarkState, ThreadState threadState)
      throws InterruptedException {
    benchmarkState.processor.onEnd(
        (ReadableSpan) benchmarkState.tracer.spanBuilder("span").startSpan());
  }
}
