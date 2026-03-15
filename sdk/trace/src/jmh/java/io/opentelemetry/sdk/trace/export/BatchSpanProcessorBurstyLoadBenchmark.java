/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Simulates bursty span production with cooldown periods to measure dropped spans and export rates
 * under high-throughput conditions.
 */
public class BatchSpanProcessorBurstyLoadBenchmark {

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private InMemoryMetricReader metricReader;
    private BatchSpanProcessor processor;
    private Tracer tracer;
    private long exportedSpans;
    private long droppedSpans;
    private double dropRatio;

    @Param({"64", "2048"})
    int maxQueueSize;

    @Param({"64"})
    int maxExportBatchSize;

    @Param({"200"})
    int scheduleDelayMs;

    @Param({"20"})
    int exporterDelayMs;

    @Param({"20000"})
    int burstSize;

    @Param({"5", "25"})
    int cooldownMs;

    @Setup(Level.Iteration)
    public void setup() {
      metricReader = InMemoryMetricReader.create();
      MeterProvider meterProvider =
          SdkMeterProvider.builder().registerMetricReader(metricReader).build();
      SpanExporter exporter = new DelayingSpanExporter(exporterDelayMs);
      processor =
          BatchSpanProcessor.builder(exporter)
              .setMeterProvider(meterProvider)
              .setMaxQueueSize(maxQueueSize)
              .setMaxExportBatchSize(maxExportBatchSize)
              .setScheduleDelay(Duration.ofMillis(scheduleDelayMs))
              .build();
      tracer = SdkTracerProvider.builder().build().get("burstyBenchmarkTracer");
    }

    @TearDown(Level.Iteration)
    public void recordMetrics() {
      processor.forceFlush().join(30, TimeUnit.SECONDS);
      BatchSpanProcessorMetrics metrics =
          new BatchSpanProcessorMetrics(metricReader.collectAllMetrics(), 1);
      dropRatio = metrics.dropRatio();
      exportedSpans = metrics.exportedSpans();
      droppedSpans = metrics.droppedSpans();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
      processor.shutdown().join(30, TimeUnit.SECONDS);
    }
  }

  @State(Scope.Thread)
  @AuxCounters(AuxCounters.Type.EVENTS)
  public static class MetricsState {
    BenchmarkState benchmarkState;

    @TearDown(Level.Iteration)
    public void capture(BenchmarkState benchmarkState) {
      this.benchmarkState = benchmarkState;
    }

    public long exportedSpans() {
      return benchmarkState.exportedSpans;
    }

    public long droppedSpans() {
      return benchmarkState.droppedSpans;
    }

    public double dropRatio() {
      return benchmarkState.dropRatio;
    }
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 3, time = 1)
  @Measurement(iterations = 5, time = 5)
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void exportBursty(
      BenchmarkState benchmarkState, @SuppressWarnings("unused") MetricsState metricsState)
      throws InterruptedException {
    for (int i = 0; i < benchmarkState.burstSize; i++) {
      Span span = benchmarkState.tracer.spanBuilder("burst-span").startSpan();
      benchmarkState.processor.onEnd((ReadableSpan) span);
    }
    if (benchmarkState.cooldownMs > 0) {
      TimeUnit.MILLISECONDS.sleep(benchmarkState.cooldownMs);
    }
  }
}
