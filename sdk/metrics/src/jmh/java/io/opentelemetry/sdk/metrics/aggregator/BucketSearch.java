/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class BucketSearch {
  private static final double[] arr5 = new double[] {5, 10, 25, 50, 100};
  private static final double[] arr10 =
      new double[] {10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120};
  private static final double[] arrLarge =
      new double[] {
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        12,
        14,
        16,
        18,
        20,
        25,
        30,
        35,
        40,
        45,
        50,
        60,
        70,
        80,
        90,
        100,
        120,
        140,
        160,
        180,
        200,
        250,
        300,
        350,
        400,
        450,
        500,
        600,
        700,
        800,
        900,
        1000,
        1200,
        1400,
        1600,
        1800,
        2000,
        2500,
        3000,
        3500,
        4000,
        4500,
        5000,
        6000,
        7000,
        8000,
        9000,
        10000,
        12000,
        14000,
        16000,
        18000,
        20000,
        25000,
        30000,
        35000,
        40000,
        45000,
        50000,
        60000,
        70000,
        80000,
        90000,
        100000,
        120000,
        140000,
        160000,
        180000,
        200000,
        250000,
        300000,
        350000,
        400000,
        450000,
        500000,
        600000,
        700000,
        800000,
        900000,
        1000000,
        1200000,
        1400000,
        1600000,
        1800000,
        2000000,
        2500000,
        3000000,
        3500000,
        4000000,
        4500000,
        5000000,
        6000000,
        7000000,
        8000000,
        9000000,
        10000000,
        12000000,
        14000000,
        16000000,
        18000000,
        20000000,
        25000000,
        30000000,
        35000000,
        40000000,
        45000000,
        50000000,
        60000000,
        70000000,
        80000000,
        90000000,
        100000000,
        120000000,
        140000000,
        160000000,
        180000000,
        200000000,
        250000000,
        300000000,
        350000000,
        400000000,
        450000000,
        500000000,
        600000000,
        700000000,
        800000000,
        900000000,
        1000000000,
        1200000000,
        1400000000,
        1600000000,
        1800000000,
        2000000000,
        2500000000.0,
        3000000000.0,
        3500000000.0,
        4000000000.0,
        4500000000.0,
        5000000000.0,
        6000000000.0,
        7000000000.0,
        8000000000.0,
        9000000000.0,
        1e200
      };

  private static int findBucketIndex(double[] values, double target) {
    for (int i = 0; i < values.length; ++i) {
      if (target < values[i]) {
        return i;
      }
    }
    return values.length;
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void linearArr5() {
    int ignored = findBucketIndex(arr5, ThreadLocalRandom.current().nextDouble(150));
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void linearArr10() {
    int ignored = findBucketIndex(arr10, ThreadLocalRandom.current().nextDouble(5000));
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void linearArrLarge() {
    int ignored = findBucketIndex(arrLarge, ThreadLocalRandom.current().nextDouble());
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void binaryArr5() {
    int ignored = Arrays.binarySearch(arr5, ThreadLocalRandom.current().nextDouble(150));
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void binaryArr10() {
    int ignored = Arrays.binarySearch(arr10, ThreadLocalRandom.current().nextDouble(5000));
  }

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Threads(value = 1)
  public void binaryArrLarge() {
    int ignored = Arrays.binarySearch(arrLarge, ThreadLocalRandom.current().nextDouble());
  }
}
