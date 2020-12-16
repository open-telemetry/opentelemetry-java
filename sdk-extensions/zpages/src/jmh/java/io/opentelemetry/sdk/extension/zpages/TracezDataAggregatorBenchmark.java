/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
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
import org.openjdk.jmh.infra.Blackhole;

/** Benchmark class for {@link TracezDataAggregator}. */
@State(Scope.Benchmark)
public class TracezDataAggregatorBenchmark {

  private static final String runningSpan = "RUNNING_SPAN";
  private static final String latencySpan = "LATENCY_SPAN";
  private static final String errorSpan = "ERROR_SPAN";
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("TracezDataAggregatorBenchmark");
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);

  @Param({"1", "10", "1000", "1000000"})
  private int numberOfSpans;

  @Setup(Level.Trial)
  public final void setup() {
    for (int i = 0; i < numberOfSpans; i++) {
      tracer.spanBuilder(runningSpan).startSpan();
      tracer.spanBuilder(latencySpan).startSpan().end();
      Span error = tracer.spanBuilder(errorSpan).startSpan();
      error.setStatus(StatusCode.ERROR);
      error.end();
    }
  }

  /** Get span counts with 1 thread. */
  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getCounts_01Thread(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpanCounts());
    blackhole.consume(dataAggregator.getSpanLatencyCounts());
    blackhole.consume(dataAggregator.getErrorSpanCounts());
  }

  /** Get span counts with 5 threads. */
  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getCounts_05Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpanCounts());
    blackhole.consume(dataAggregator.getSpanLatencyCounts());
    blackhole.consume(dataAggregator.getErrorSpanCounts());
  }

  /** Get span counts with 10 threads. */
  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getCounts_10Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpanCounts());
    blackhole.consume(dataAggregator.getSpanLatencyCounts());
    blackhole.consume(dataAggregator.getErrorSpanCounts());
  }

  /** Get span counts with 20 threads. */
  @Benchmark
  @Threads(value = 20)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getCounts_20Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpanCounts());
    blackhole.consume(dataAggregator.getSpanLatencyCounts());
    blackhole.consume(dataAggregator.getErrorSpanCounts());
  }

  /** Get spans with 1 thread. */
  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getSpans_01Thread(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpans(runningSpan));
    blackhole.consume(dataAggregator.getOkSpans(latencySpan, 0, Long.MAX_VALUE));
    blackhole.consume(dataAggregator.getErrorSpans(errorSpan));
  }

  /** Get spans with 5 threads. */
  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getSpans_05Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpans(runningSpan));
    blackhole.consume(dataAggregator.getOkSpans(latencySpan, 0, Long.MAX_VALUE));
    blackhole.consume(dataAggregator.getErrorSpans(errorSpan));
  }

  /** Get spans with 10 threads. */
  @Benchmark
  @Threads(value = 10)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getSpans_10Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpans(runningSpan));
    blackhole.consume(dataAggregator.getOkSpans(latencySpan, 0, Long.MAX_VALUE));
    blackhole.consume(dataAggregator.getErrorSpans(errorSpan));
  }

  /** Get spans with 20 threads. */
  @Benchmark
  @Threads(value = 20)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void getSpans_20Threads(Blackhole blackhole) {
    blackhole.consume(dataAggregator.getRunningSpans(runningSpan));
    blackhole.consume(dataAggregator.getOkSpans(latencySpan, 0, Long.MAX_VALUE));
    blackhole.consume(dataAggregator.getErrorSpans(errorSpan));
  }
}
