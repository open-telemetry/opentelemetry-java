/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
import org.openjdk.jmh.annotations.Warmup;

/** Measures runtime cost of computing bucket indexes for exponential histograms. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(1)
public class ExponentialHistogramIndexerBenchmark {

  @State(Scope.Thread)
  public static class ThreadState {
    @Param(value = {"1", "0", "-1"})
    int scale;

    private double[] values;
    private final AtomicLong valueIndex = new AtomicLong();
    private Base2ExponentialHistogramIndexer indexer;

    @Setup(Level.Trial)
    public final void setup() {
      Random random = new Random();
      int numValues = 2000;
      values = new double[numValues];
      for (int i = 0; i < numValues; i++) {
        values[i] = random.nextDouble() * 1000;
      }
      indexer = Base2ExponentialHistogramIndexer.get(scale);
    }

    public void compute() {
      // Compute a number of samples
      for (int i = 0; i < 2000; i++) {
        indexer.computeIndex(values[(int) (valueIndex.incrementAndGet() % values.length)]);
      }
    }
  }

  @Benchmark
  public void computeIndex(ThreadState threadState) {
    threadState.compute();
  }
}
