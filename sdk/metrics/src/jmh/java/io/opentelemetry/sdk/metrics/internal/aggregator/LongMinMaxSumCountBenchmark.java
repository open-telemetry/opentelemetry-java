/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class LongMinMaxSumCountBenchmark {
  private static final Aggregator<MinMaxSumCountAccumulation> aggregator =
      new LongMinMaxSumCountAggregator(ExemplarReservoir::noSamples);
  private AggregatorHandle<MinMaxSumCountAccumulation> aggregatorHandle;

  @Setup(Level.Trial)
  public final void setup() {
    aggregatorHandle = aggregator.createHandle();
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Threads(value = 10)
  public void aggregate_10Threads() {
    aggregatorHandle.recordLong(100);
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Threads(value = 5)
  public void aggregate_5Threads() {
    aggregatorHandle.recordLong(100);
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Threads(value = 1)
  public void aggregate_1Threads() {
    aggregatorHandle.recordLong(100);
  }
}
