/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
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

public class BatchSpanProcessorDroppedSpansBenchmark {

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
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private final MetricProducer metricProducer =
        ((SdkMeterProvider) GlobalMetricsProvider.get()).getMetricProducer();
    private BatchSpanProcessor processor;
    private Tracer tracer;
    private Collection<MetricData> allMetrics;

    @Setup(Level.Trial)
    public final void setup() {
      SpanExporter exporter = new DelayingSpanExporter();
      processor = BatchSpanProcessor.builder(exporter).build();

      tracer = SdkTracerProvider.builder().build().get("benchmarkTracer");
    }

    @TearDown(Level.Trial)
    public final void tearDown() {
      processor.shutdown();
    }

    @TearDown(Level.Iteration)
    public final void recordMetrics() {
      allMetrics = metricProducer.collectAllMetrics();
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
      long exported = getMetric(true);
      long dropped = getMetric(false);
      long total = exported + dropped;
      if (total == 0) {
        return 0;
      } else {
        // Due to peculiarities of JMH reporting we have to divide this by the number of the
        // concurrent threads running the actual benchmark.
        return (double) dropped / total / 5;
      }
    }

    public long exportedSpans() {
      return getMetric(true);
    }

    public long droppedSpans() {
      return getMetric(false);
    }

    private long getMetric(boolean dropped) {
      String labelValue = String.valueOf(dropped);
      for (MetricData metricData : allMetrics) {
        if (metricData.getName().equals("processedSpans")) {
          List<Point> points = new ArrayList<>(metricData.getPoints());
          if (points.isEmpty()) {
            return 0;
          } else {
            // Find latest point with given value of dropped label
            for (int i = points.size() - 1; i >= 0; i--) {
              LongPoint point = (LongPoint) points.get(i);
              if (labelValue.equals(point.getLabels().get("dropped"))) {
                return point.getValue();
              }
            }
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
  public void export(
      BenchmarkState benchmarkState, @SuppressWarnings("unused") ThreadState threadState) {
    benchmarkState.processor.onEnd(
        (ReadableSpan) benchmarkState.tracer.spanBuilder("span").startSpan());
  }
}
