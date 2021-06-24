/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import java.util.Arrays;
import org.assertj.core.api.Assertions;

/** Assertions for an exported {@link DoubleHistogramPointData}. */
public class DoubleHistogramPointDataAssert
    extends AbstractSampledPointDataAssert<
        DoubleHistogramPointDataAssert, DoubleHistogramPointData> {

  protected DoubleHistogramPointDataAssert(DoubleHistogramPointData actual) {
    super(actual, DoubleHistogramPointDataAssert.class);
  }

  public DoubleHistogramPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  public DoubleHistogramPointDataAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  public DoubleHistogramPointDataAssert hasBucketBoundaries(double... boundaries) {
    isNotNull();
    Double[] bigBoundaries =
        Arrays.stream(boundaries).mapToObj(Double::valueOf).toArray(idx -> new Double[idx]);
    Assertions.assertThat(actual.getBoundaries()).as("boundaries").containsExactly(bigBoundaries);
    return this;
  }

  public DoubleHistogramPointDataAssert hasBucketCounts(long... counts) {
    isNotNull();
    Long[] bigCounts = Arrays.stream(counts).mapToObj(Long::valueOf).toArray(idx -> new Long[idx]);
    Assertions.assertThat(actual.getCounts()).as("bucketCounts").containsExactly(bigCounts);
    return this;
  }
}
