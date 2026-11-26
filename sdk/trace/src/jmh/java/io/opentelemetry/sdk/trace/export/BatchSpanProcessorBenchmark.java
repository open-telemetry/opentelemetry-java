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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/** Burst-and-flush workload, including span creation/end and a complete span accounting. */
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BatchSpanProcessorBenchmark {
  @Param({"1000", "2000", "5000"})
  public int spanCount;

  @Benchmark
  @Threads(5)
  public void export(BatchSpanProcessorBenchmarkState state, Counters counters) {
    for (int i = 0; i < spanCount; i++) {
      state.endSpan(counters);
    }
    state.flush();
  }
}
