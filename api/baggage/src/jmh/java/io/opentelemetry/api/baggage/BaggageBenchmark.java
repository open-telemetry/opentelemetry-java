/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("JavadocMethod")
@State(Scope.Thread)
public class BaggageBenchmark {

  @Param({"0", "1", "10", "100"})
  public int itemsToAdd;

  private List<String> keys;
  private List<String> values;

  @Setup
  public void setUp() {
    keys = new ArrayList<>(itemsToAdd);
    values = new ArrayList<>(itemsToAdd);

    // pre-allocate the keys & values to remove one possible confounding factor
    for (int i = 0; i < itemsToAdd; i++) {
      keys.add("key" + i);
      values.add("value" + i);
    }
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Baggage baggageItemBenchmark() {
    BaggageBuilder builder = Baggage.builder();
    for (int i = 0; i < itemsToAdd; i++) {
      builder.put(keys.get(i), values.get(i));
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
      baggage = baggage.toBuilder().put(keys.get(i), values.get(i)).build();
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
      baggage = Baggage.builder().put(keys.get(i), values.get(i)).setParent(context).build();
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
