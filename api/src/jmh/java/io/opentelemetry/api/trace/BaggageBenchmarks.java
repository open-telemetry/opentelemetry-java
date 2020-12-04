/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.context.Context;
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
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("JavadocMethod")
@State(Scope.Thread)
public class BaggageBenchmarks {

  @Param({"0", "1", "10", "100"})
  public int itemsToAdd;

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Baggage baggageItemBenchmark() {
    BaggageBuilder builder = Baggage.builder();
    for (int i = 0; i < itemsToAdd; i++) {
      builder.put("key" + i, "value" + i);
    }
    return builder.build();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Baggage baggageToBuilderBenchmark() {
    Baggage baggage = Baggage.empty();
    for (int i = 0; i < itemsToAdd; i++) {
      baggage = baggage.toBuilder().put("key" + i, "value" + i).build();
    }
    return baggage;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Baggage baggageParentBenchmark() {
    Baggage baggage = Baggage.empty();
    Context context = Context.root().with(baggage);
    for (int i = 0; i < itemsToAdd; i++) {
      baggage = Baggage.builder().put("key" + i, "value" + i).setParent(context).build();
      context = context.with(baggage);
    }
    return baggage;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Baggage baggageParentBenchmark_noContent() {
    Baggage baggage = Baggage.empty();
    Context context = Context.root().with(baggage);
    for (int i = 0; i < itemsToAdd; i++) {
      baggage = Baggage.builder().setParent(context).build();
      context = context.with(baggage);
    }
    return baggage;
  }
}
