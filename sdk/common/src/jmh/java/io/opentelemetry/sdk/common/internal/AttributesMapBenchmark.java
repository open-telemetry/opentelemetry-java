/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.infra.Blackhole;
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

/**
 * Microbenchmark for {@link AttributesMap}. Covers writes ({@code put}) and reads ({@code get},
 * {@code forEach}), parametrized by number of attributes.
 *
 * <p>Write scenarios:
 *
 * <ul>
 *   <li>{@code uniqueKeys} — normal case: each key name is distinct
 *   <li>{@code sameKeySameType} — repeated put with the same AttributeKey (pure overwrite)
 *   <li>{@code sameKeyDifferentType} — regression benchmark: same string name cycling through four
 *       types (pre-fix: produces N entries; post-fix: produces 1 entry)
 *   <li>{@code mixedUniqueAndOverwrite} — primary realistic case: unique keys followed by
 *       same-name overwrites with a different type, exactly {@code numAttributes} puts total
 * </ul>
 *
 * <p>Read scenarios (run on a pre-filled map of {@code numAttributes} unique string entries):
 *
 * <ul>
 *   <li>{@code getHit} — lookup with the exact stored key type (hit)
 *   <li>{@code getTypeMiss} — lookup with a different type for the same key name (miss by type;
 *       behaviour changes after the fix: post-fix only one entry exists per name)
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
  private List<AttributeKey<Long>> longKeys;
  private List<AttributeKey<Double>> doubleKeys;
  private List<String> values;

  // Pre-filled map used by read benchmarks — populated once in @Setup.
  private AttributesMap filledMap;

  @Setup
  public void setup() {
    stringKeys = new ArrayList<>(numAttributes);
    boolKeys = new ArrayList<>(numAttributes);
    longKeys = new ArrayList<>(numAttributes);
    doubleKeys = new ArrayList<>(numAttributes);
    values = new ArrayList<>(numAttributes);
    for (int i = 0; i < numAttributes; i++) {
      stringKeys.add(stringKey("key" + i));
      boolKeys.add(booleanKey("key" + i));
      longKeys.add(longKey("key" + i));
      doubleKeys.add(doubleKey("key" + i));
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

  /** Same AttributeKey reused — standard same-type overwrite path. Capacity=1 since only one
   * entry ever occupies the map. */
  @Benchmark
  public AttributesMap sameKeySameType() {
    AttributesMap map = AttributesMap.create(1, Integer.MAX_VALUE);
    AttributeKey<String> key = stringKeys.get(0);
    for (int i = 0; i < numAttributes; i++) {
      map.put(key, values.get(i));
    }
    return map;
  }

  /**
   * Same string key name, cycling through string → boolean → long → double → string → ...
   *
   * <p>Regression benchmark: pre-fix produces up to 4 entries (one per type); post-fix produces 1
   * entry (last-value-wins). Capacity=4 matches the number of distinct types in the cycle.
   */
  @Benchmark
  public AttributesMap sameKeyDifferentType() {
    AttributesMap map = AttributesMap.create(4, Integer.MAX_VALUE);
    for (int i = 0; i < numAttributes; i++) {
      switch (i % 4) {
        case 0:
          map.put(stringKeys.get(0), values.get(0));
          break;
        case 1:
          map.put(boolKeys.get(0), true);
          break;
        case 2:
          map.put(longKeys.get(0), 42L);
          break;
        default:
          map.put(doubleKeys.get(0), 3.14);
          break;
      }
    }
    return map;
  }

  /**
   * Realistic mixed case: first {@code numAttributes/2} puts insert unique string keys, the
   * remaining puts overwrite those same keys with boolean values (different type). Always performs
   * exactly {@code numAttributes} put operations total.
   */
  @Benchmark
  public AttributesMap mixedUniqueAndOverwrite() {
    int first = numAttributes / 2;
    int second = numAttributes - first;
    AttributesMap map = AttributesMap.create(numAttributes, Integer.MAX_VALUE);
    for (int i = 0; i < first; i++) {
      map.put(stringKeys.get(i), values.get(i));
    }
    for (int i = 0; i < second; i++) {
      map.put(boolKeys.get(i), i % 2 == 0);
    }
    return map;
  }

  // ---- Read benchmarks (operate on pre-filled map) ----

  /**
   * Lookup with the exact stored key type — always a hit. Measures the cost of a successful
   * {@code get()} for each entry in the map.
   */
  @Benchmark
  public void getHit(Blackhole bh) {
    for (int i = 0; i < numAttributes; i++) {
      bh.consume(filledMap.get(stringKeys.get(i)));
    }
  }

  /**
   * Lookup with a different type for the same key name — always a type-miss (returns null).
   *
   * <p>Pre-fix: map contains N string entries; boolean keys simply miss the HashMap. Post-fix: map
   * contains N string entries; boolean key finds the entry but type check returns null. Isolates
   * the type-check overhead introduced by the fix.
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
}
