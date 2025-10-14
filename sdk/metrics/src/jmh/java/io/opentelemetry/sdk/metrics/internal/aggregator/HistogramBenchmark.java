/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/** Measures runtime cost of histogram aggregations. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 10, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(1)
public class HistogramBenchmark {

  @State(Scope.Thread)
  public static class ThreadState {
    @Param HistogramValueGenerator valueGen;
    @Param HistogramAggregationParam aggregation;
    private AggregatorHandle<?> aggregatorHandle;
    private DoubleSupplier valueSupplier;

    @Setup(Level.Trial)
    public final void setup() {
      aggregatorHandle = aggregation.getAggregator().createHandle();
      valueSupplier = valueGen.supplier();
    }

    public void record() {
      // Record a number of samples.
      for (int i = 0; i < 2000; i++) {
        this.aggregatorHandle.recordDouble(
            valueSupplier.getAsDouble(), Attributes.empty(), Context.current());
      }
    }
  }

  @Benchmark
  @Threads(value = 10)
  public void aggregate_10Threads(ThreadState threadState) {
    threadState.record();
  }

  @Benchmark
  @Threads(value = 5)
  public void aggregate_5Threads(ThreadState threadState) {
    threadState.record();
  }

  @Benchmark
  @Threads(value = 1)
  public void aggregate_1Threads(ThreadState threadState) {
    threadState.record();
  }
}
