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

/**
 * Attempts to measure the cost of re-scaling/building buckets.
 *
 * <p>This benchmark must be interpreted carefully. We're looking for startup costs of histograms
 * and need to tease out the portion of recorded time from scaling buckets vs. general algorithmic
 * performance. The difference, compared with HistogramBenchmark, is that setup is called before
 * each invocation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 10, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(1)
public class HistogramScaleBenchmark {
  @State(Scope.Thread)
  public static class ThreadState {
    @Param HistogramValueGenerator valueGen;
    @Param HistogramAggregationParam aggregation;
    private AggregatorHandle<?> aggregatorHandle;
    private DoubleSupplier valueSupplier;

    @Setup(Level.Invocation)
    public final void setup() {
      aggregatorHandle = aggregation.getAggregator().createHandle();
      valueSupplier = valueGen.supplier();
    }

    public void record() {
      // Record a number of samples.
      for (int i = 0; i < 20000; i++) {
        this.aggregatorHandle.recordDouble(
            valueSupplier.getAsDouble(), Attributes.empty(), Context.current());
      }
    }
  }

  @Benchmark
  @Threads(value = 1)
  public void scaleUp(ThreadState threadState) {
    threadState.record();
  }
}
