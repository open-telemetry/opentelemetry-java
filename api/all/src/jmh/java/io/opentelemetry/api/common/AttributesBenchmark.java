/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("JavadocMethod")
@State(Scope.Thread)
public class AttributesBenchmark {

  // pre-allocate the keys & values to remove one possible confounding factor
  private static final List<AttributeKey<String>> keys = new ArrayList<>(10);
  private static final List<String> values = new ArrayList<>(10);

  static {
    for (int i = 0; i < 10; i++) {
      keys.add(AttributeKey.stringKey("key" + i));
      values.add("value" + i);
    }
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes ofOne() {
    return Attributes.of(keys.get(0), values.get(0));
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes ofTwo() {
    return Attributes.of(keys.get(0), values.get(0), keys.get(1), values.get(1));
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes ofThree() {
    return Attributes.of(
        keys.get(0), values.get(0), keys.get(1), values.get(1), keys.get(2), values.get(2));
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes ofFour() {
    return Attributes.of(
        keys.get(0),
        values.get(0),
        keys.get(1),
        values.get(1),
        keys.get(2),
        values.get(2),
        keys.get(3),
        values.get(3));
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes ofFive() {
    return Attributes.of(
        keys.get(0),
        values.get(0),
        keys.get(1),
        values.get(1),
        keys.get(2),
        values.get(2),
        keys.get(3),
        values.get(3),
        keys.get(4),
        values.get(4));
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @Fork(1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(iterations = 5, time = 1)
  public Attributes builderTenItems() {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (int i = 0; i < 10; i++) {
      attributesBuilder.put(keys.get(i), values.get(i));
    }
    return attributesBuilder.build();
  }
}
