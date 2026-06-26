/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
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
import org.openjdk.jmh.infra.Blackhole;

/**
 * Microbenchmark for {@link AttributesMap}. Parametrized by number of attributes.
 *
 * <p>Write scenarios:
 *
 * <ul>
 *   <li>{@code uniqueKeys} — normal production case: each key name is distinct
 *   <li>{@code putThenForEach} — combined write + export cycle: N unique puts followed by one
 *       {@code forEach}, modeling the dominant span lifecycle
 * </ul>
 *
 * <p>Read scenarios (run on a pre-filled map of {@code numAttributes} unique string entries):
 *
 * <ul>
 *   <li>{@code getHit} — lookup with the exact stored key type (hit)
 *   <li>{@code getTypeMiss} — lookup with a different type for the same key name (type miss)
 *   <li>{@code forEachAll} — full iteration over all entries
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class AttributesMapBenchmark {

  @Param({"4", "16", "128"})
  int numAttributes;

  private List<AttributeKey<String>> stringKeys;
  private List<AttributeKey<Boolean>> boolKeys;
  private List<String> values;

  // Pre-filled map used by read benchmarks — populated once in @Setup.
  private AttributesMap filledMap;

  @Setup
  public void setup() {
    stringKeys = new ArrayList<>(numAttributes);
    boolKeys = new ArrayList<>(numAttributes);
    values = new ArrayList<>(numAttributes);
    for (int i = 0; i < numAttributes; i++) {
      stringKeys.add(stringKey("key" + i));
      boolKeys.add(booleanKey("key" + i));
      values.add("value" + i);
    }
    filledMap = AttributesMap.create(numAttributes, Integer.MAX_VALUE);
    for (int i = 0; i < numAttributes; i++) {
      filledMap.put(stringKeys.get(i), values.get(i));
    }
  }

  /** Each key name is unique — the common production case. */
  @Benchmark
  public AttributesMap uniqueKeys() {
    AttributesMap map = AttributesMap.create(numAttributes, Integer.MAX_VALUE);
    for (int i = 0; i < numAttributes; i++) {
      map.put(stringKeys.get(i), values.get(i));
    }
    return map;
  }

  // ---- Read benchmarks (operate on pre-filled map) ----

  /**
   * Lookup with the exact stored key type — always a hit. Measures the cost of a successful {@code
   * get()} for each entry in the map.
   */
  @Benchmark
  public void getHit(Blackhole bh) {
    for (int i = 0; i < numAttributes; i++) {
      bh.consume(filledMap.get(stringKeys.get(i)));
    }
  }

  /**
   * Lookup with a different type for the same key name — always returns null.
   *
   * <p>The map holds N string-typed entries; boolean keys for the same names locate each entry by
   * name but fail the type check. Isolates the cost of a name-hit / type-miss lookup.
   */
  @Benchmark
  public void getTypeMiss(Blackhole bh) {
    for (int i = 0; i < numAttributes; i++) {
      bh.consume(filledMap.get(boolKeys.get(i)));
    }
  }

  /** Full iteration over all entries via {@code forEach}. */
  @Benchmark
  public void forEachAll(Blackhole bh) {
    filledMap.forEach((k, v) -> bh.consume(v));
  }

  /**
   * Combined write + read cycle: fill a fresh map with N unique string keys, then iterate all
   * entries once. Models the dominant production path: N puts during span building, followed by one
   * forEach at export time.
   */
  @Benchmark
  public void putThenForEach(Blackhole bh) {
    AttributesMap map = AttributesMap.create(numAttributes, Integer.MAX_VALUE);
    for (int i = 0; i < numAttributes; i++) {
      map.put(stringKeys.get(i), values.get(i));
    }
    map.forEach((k, v) -> bh.consume(v));
  }
}
