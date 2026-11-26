/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBenchmarkState.Counters;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/** Closed-loop span lifecycle throughput. Inspect exported/dropped counts alongside attempts. */
@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BatchSpanProcessorMultiThreadBenchmark {
  @Benchmark
  @Threads(1)
  public void export_01Thread(BatchSpanProcessorBenchmarkState state, Counters counters) {
    state.endSpan(counters);
  }

  @Benchmark
  @Threads(2)
  public void export_02Thread(BatchSpanProcessorBenchmarkState state, Counters counters) {
    state.endSpan(counters);
  }

  @Benchmark
  @Threads(5)
  public void export_05Thread(BatchSpanProcessorBenchmarkState state, Counters counters) {
    state.endSpan(counters);
  }

  @Benchmark
  @Threads(10)
  public void export_10Thread(BatchSpanProcessorBenchmarkState state, Counters counters) {
    state.endSpan(counters);
  }

  @Benchmark
  @Threads(20)
  public void export_20Thread(BatchSpanProcessorBenchmarkState state, Counters counters) {
    state.endSpan(counters);
  }
}
