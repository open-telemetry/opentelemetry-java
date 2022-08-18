/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

class ExponentialHistogramIndexerTest {

  @Test
  void computeIndex_ScaleOne() {
    ExponentialHistogramIndexer indexer = new ExponentialHistogramIndexer(1);

    Arrays.asList(
            of(15d, 7),
            of(9d, 6),
            of(7d, 5),
            of(5d, 4),
            of(3d, 3),
            of(2.5d, 2),
            of(1.5d, 1),
            of(1.2d, 0),
            of(1d, -1),
            of(0.75d, -1),
            of(0.55d, -2),
            of(0.45d, -3))
        .forEach(test(indexer));
  }

  @Test
  void computeIndex_ScaleZero() {
    ExponentialHistogramIndexer indexer = new ExponentialHistogramIndexer(0);

    Arrays.asList(
            // Near +Inf
            of(Double.MAX_VALUE, Double.MAX_EXPONENT),
            of(0x1p1023, 1022),
            of(0x1.1p1023, 1023),
            of(0x1p1022, 1021),
            of(0x1.1p1022, 1022),
            // Near 0
            of(0x1p-1022, -1023),
            of(0x1.1p-1022, -1022),
            of(0x1p-1021, -1022),
            of(0x1.1p-1021, -1021),
            of(Double.MIN_NORMAL, -1023),
            of(Double.MIN_VALUE, -1023),
            // Near 1
            of(4, 1),
            of(3, 1),
            of(2, 0),
            of(1.5, 0),
            of(1, -1),
            of(0.75, -1),
            of(0.51, -1),
            of(0.5, -2),
            of(0.26, -2),
            of(0.25, -3),
            of(0.126, -3),
            of(0.125, -4))
        .forEach(test(indexer));
  }

  @Test
  void computeIndex_ScaleNegOne() {
    ExponentialHistogramIndexer indexer = new ExponentialHistogramIndexer(-1);

    Arrays.asList(
            of(17d, 2),
            of(16d, 1),
            of(15d, 1),
            of(9d, 1),
            of(8d, 1),
            of(5d, 1),
            of(4d, 0),
            of(3d, 0),
            of(2d, 0),
            of(1.5d, 0),
            of(1d, -1),
            of(0.75d, -1),
            of(0.5d, -1),
            of(0.25d, -2),
            of(0.20d, -2),
            of(0.13d, -2),
            of(0.125d, -2),
            of(0.10d, -2),
            of(0.0625d, -3),
            of(0.06d, -3))
        .forEach(test(indexer));
  }

  @Test
  void valueToIndex_ScaleNegFour() {
    ExponentialHistogramIndexer indexer = new ExponentialHistogramIndexer(-4);

    Arrays.asList(
            of(0x1p0, -1),
            of(0x10p0, 0),
            of(0x100p0, 0),
            of(0x1000p0, 0),
            of(0x10000p0, 0), // Base == 2**16
            of(0x100000p0, 1),
            of(0x1000000p0, 1),
            of(0x10000000p0, 1),
            of(0x100000000p0, 1), // == 2**32
            of(0x1000000000p0, 2),
            of(0x10000000000p0, 2),
            of(0x100000000000p0, 2),
            of(0x1000000000000p0, 2), // 2**48
            of(0x10000000000000p0, 3),
            of(0x100000000000000p0, 3),
            of(0x1000000000000000p0, 3),
            of(0x10000000000000000p0, 3), // 2**64
            of(0x100000000000000000p0, 4),
            of(0x1000000000000000000p0, 4),
            of(0x10000000000000000000p0, 4),
            of(0x100000000000000000000p0, 4),
            of(0x1000000000000000000000p0, 5),
            of(1 / 0x1p0, -1),
            of(1 / 0x10p0, -1),
            of(1 / 0x100p0, -1),
            of(1 / 0x1000p0, -1),
            of(1 / 0x10000p0, -2), // 2**-16
            of(1 / 0x100000p0, -2),
            of(1 / 0x1000000p0, -2),
            of(1 / 0x10000000p0, -2),
            of(1 / 0x100000000p0, -3), // 2**-32
            of(1 / 0x1000000000p0, -3),
            of(1 / 0x10000000000p0, -3),
            of(1 / 0x100000000000p0, -3),
            of(1 / 0x1000000000000p0, -4), // 2**-48
            of(1 / 0x10000000000000p0, -4),
            of(1 / 0x100000000000000p0, -4),
            of(1 / 0x1000000000000000p0, -4),
            of(1 / 0x10000000000000000p0, -5), // 2**-64
            of(1 / 0x100000000000000000p0, -5),

            // Max values
            of(0x1.FFFFFFFFFFFFFp1023, 63),
            of(0x1p1023, 63),
            of(0x1p1019, 63),
            of(0x1p1009, 63),
            of(0x1p1008, 62),
            of(0x1p1007, 62),
            of(0x1p1000, 62),
            of(0x1p0993, 62),
            of(0x1p0992, 61),
            of(0x1p0991, 61),

            // Min and subnormal values
            of(0x1p-1074, -64),
            of(0x1p-1073, -64),
            of(0x1p-1072, -64),
            of(0x1p-1057, -64),
            of(0x1p-1056, -64),
            of(0x1p-1041, -64),
            of(0x1p-1040, -64),
            of(0x1p-1025, -64),
            of(0x1p-1024, -64),
            of(0x1p-1023, -64),
            of(0x1p-1022, -64),
            of(0x1p-1009, -64),
            of(0x1p-1008, -64),
            of(0x1p-1007, -63),
            of(0x1p-0993, -63),
            of(0x1p-0992, -63),
            of(0x1p-0991, -62),
            of(0x1p-0977, -62),
            of(0x1p-0976, -62),
            of(0x1p-0975, -61))
        .forEach(test(indexer));
  }

  private static Consumer<TestCase> test(ExponentialHistogramIndexer indexer) {
    return testCase ->
        AssertionsForClassTypes.assertThat(indexer.computeIndex(testCase.value))
            .describedAs(
                "value " + Double.toHexString(testCase.value) + " (" + testCase.value + ")")
            .isEqualTo(testCase.expectedBucket);
  }

  private static TestCase of(double value, int expectedBucket) {
    return new TestCase(value, expectedBucket);
  }

  private static class TestCase {
    private final double value;
    private final int expectedBucket;

    private TestCase(double value, int expectedBucket) {
      this.value = value;
      this.expectedBucket = expectedBucket;
    }
  }
}
