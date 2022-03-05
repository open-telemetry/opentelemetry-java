/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.Arrays;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link HistogramPointData}. */
public class HistogramPointDataAssert
    extends AbstractPointDataAssert<HistogramPointDataAssert, HistogramPointData> {

  protected HistogramPointDataAssert(HistogramPointData actual) {
    super(actual, HistogramPointDataAssert.class);
  }

  /** Ensures the {@code sum} field matches the expected value. */
  public HistogramPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code sum} field contains a greater value than the passed {@code boundary}. */
  public HistogramPointDataAssert hasSumGreaterThan(double boundary) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isGreaterThan(boundary);
    return this;
  }

  /** Ensures the {@code count} field matches the expected value. */
  public HistogramPointDataAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /**
   * Ensures the {@code boundaries} field matches the expected value.
   *
   * @param boundaries The set of bucket boundaries in the same order as the expected collection.
   */
  public HistogramPointDataAssert hasBucketBoundaries(double... boundaries) {
    isNotNull();
    Double[] bigBoundaries = Arrays.stream(boundaries).boxed().toArray(Double[]::new);
    Assertions.assertThat(actual.getBoundaries()).as("boundaries").containsExactly(bigBoundaries);
    return this;
  }

  /**
   * Ensures the {@code counts} field matches the expected value.
   *
   * @param counts The set of bucket counts in the same order as the expected collection.
   */
  public HistogramPointDataAssert hasBucketCounts(long... counts) {
    isNotNull();
    Long[] bigCounts = Arrays.stream(counts).boxed().toArray(Long[]::new);
    Assertions.assertThat(actual.getCounts()).as("bucketCounts").containsExactly(bigCounts);
    return this;
  }
}
