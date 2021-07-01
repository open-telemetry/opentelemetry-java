/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import java.util.Arrays;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link DoubleHistogramPointData}. */
public class DoubleHistogramPointDataAssert
    extends AbstractPointDataAssert<DoubleHistogramPointDataAssert, DoubleHistogramPointData> {

  protected DoubleHistogramPointDataAssert(DoubleHistogramPointData actual) {
    super(actual, DoubleHistogramPointDataAssert.class);
  }

  /** Ensures the {@code sum} field matches the expected value. */
  public DoubleHistogramPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code count} field matches the expected value. */
  public DoubleHistogramPointDataAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /**
   * Ensures the {@code boundaries} field matches the expected value.
   *
   * @param boundaries The set of bucket boundaries in the same order as the expected collection.
   */
  public DoubleHistogramPointDataAssert hasBucketBoundaries(double... boundaries) {
    isNotNull();
    Double[] bigBoundaries =
        Arrays.stream(boundaries).mapToObj(Double::valueOf).toArray(idx -> new Double[idx]);
    Assertions.assertThat(actual.getBoundaries()).as("boundaries").containsExactly(bigBoundaries);
    return this;
  }

  /**
   * Ensures the {@code counts} field matches the expected value.
   *
   * @param counts The set of bucket counts in the same order as the expected collection.
   */
  public DoubleHistogramPointDataAssert hasBucketCounts(long... counts) {
    isNotNull();
    Long[] bigCounts = Arrays.stream(counts).mapToObj(Long::valueOf).toArray(idx -> new Long[idx]);
    Assertions.assertThat(actual.getCounts()).as("bucketCounts").containsExactly(bigCounts);
    return this;
  }
}
