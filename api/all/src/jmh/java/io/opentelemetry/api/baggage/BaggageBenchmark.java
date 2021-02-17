/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

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
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("JavadocMethod")
@State(Scope.Thread)
public class BaggageBenchmark {

  @Param({"0", "1", "10", "100"})
  public int itemsToAdd;

  // pre-allocate the keys & values to remove one possible confounding factor
  private static final List<String> keys = new ArrayList<>(100);
  private static final List<String> values = new ArrayList<>(100);

  static {
    for (int i = 0; i < 100; i++) {
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
}
