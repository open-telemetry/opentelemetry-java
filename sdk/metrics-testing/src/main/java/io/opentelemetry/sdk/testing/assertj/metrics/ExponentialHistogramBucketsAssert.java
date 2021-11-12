/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.List;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link ExponentialHistogramBuckets}. */
public class ExponentialHistogramBucketsAssert
    extends AbstractAssert<ExponentialHistogramBucketsAssert, ExponentialHistogramBuckets> {

  protected ExponentialHistogramBucketsAssert(ExponentialHistogramBuckets actual) {
    super(actual, ExponentialHistogramBucketsAssert.class);
  }

  /**
   * Ensures that the {@code counts} field matches the expected value.
   *
   * @param expected The bucket counts.
   */
  public ExponentialHistogramBucketsAssert hasCounts(List<Long> expected) {
    isNotNull();
    Assertions.assertThat(actual.getBucketCounts()).as("bucketCounts").isEqualTo(expected);
    return this;
  }

  /**
   * Ensures that the {@code totalCount} field matches the expected value.
   *
   * @param expected The expected total count.
   */
  public ExponentialHistogramBucketsAssert hasTotalCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getTotalCount()).as("totalCount").isEqualTo(expected);
    return this;
  }

  /**
   * Ensures that the {@code totalCount} field matches the expected value.
   *
   * @param expected The expected total count.
   */
  public ExponentialHistogramBucketsAssert hasOffset(int expected) {
    isNotNull();
    Assertions.assertThat(actual.getOffset()).as("offset").isEqualTo(expected);
    return this;
  }
}
