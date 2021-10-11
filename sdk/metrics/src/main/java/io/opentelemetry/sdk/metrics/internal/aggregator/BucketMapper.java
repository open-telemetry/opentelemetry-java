/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

/**
 * Interface defining methods for mapping values to buckets for {@link
 * io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets}.
 */
interface BucketMapper {
  /**
   * Maps a recorded double value to a bucket index. If the index falls out of the range that can
   * be represented by an int, then it is expected that the histogram would be downscaled.
   *
   * @param value Measured value
   * @return the index of the bucket which the value maps to.
   */
  long valueToIndex(double value);
}
