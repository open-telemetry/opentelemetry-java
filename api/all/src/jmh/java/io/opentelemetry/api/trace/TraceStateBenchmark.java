/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Measurement(iterations = 15, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
public class TraceStateBenchmark {

  @Benchmark
  public TraceState oneItem() {
    TraceStateBuilder builder = TraceState.builder();
    builder.put("key1", "val");
    return builder.build();
  }

  @Benchmark
  public TraceState fiveItems() {
    TraceStateBuilder builder = TraceState.builder();
    builder.put("key1", "val");
    builder.put("key2", "val");
    builder.put("key3", "val");
    builder.put("key4", "val");
    builder.put("key5", "val");
    return builder.build();
  }

  @Benchmark
  public TraceState fiveItemsWithRemoval() {
    TraceStateBuilder builder = TraceState.builder();
    builder.put("key1", "val");
    builder.put("key2", "val");
    builder.put("key3", "val");
    builder.remove("key2");
    builder.remove("key3");
    builder.put("key2", "val");
    builder.put("key3", "val");
    builder.put("key4", "val");
    builder.put("key5", "val");
    return builder.build();
  }
}
