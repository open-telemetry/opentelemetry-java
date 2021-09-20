/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
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
public class DoubleHistogramBenchmark {
  private static final Aggregator<HistogramAccumulation> aggregator =
      AggregatorFactory.histogram(Arrays.asList(10.0, 100.0, 1_000.0), AggregationTemporality.DELTA)
          .create(
              Resource.getDefault(),
              InstrumentationLibraryInfo.empty(),
              InstrumentDescriptor.create(
                  "name", "description", "1", InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE),
              MetricDescriptor.create("name", "description", "1"),
              ExemplarReservoir::noSamples);
  private AggregatorHandle<HistogramAccumulation> aggregatorHandle;

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
    aggregatorHandle.recordDouble(100.0056);
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Threads(value = 5)
  public void aggregate_5Threads() {
    aggregatorHandle.recordDouble(100.0056);
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Threads(value = 1)
  public void aggregate_1Threads() {
    aggregatorHandle.recordDouble(100.0056);
  }
}
