/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * ExponentialHistogramBuckets represents either the positive or negative measurements taken for a
 * {@link ExponentialHistogramPointData}.
 *
 * <p>The bucket boundaries are lower-bound inclusive, and are calculated using the {@link
 * ExponentialHistogramPointData#getScale()} and the {@link #getOffset()}.
 *
 * <p>For example, assume {@link ExponentialHistogramPointData#getScale()} is 0, implying {@link
 * ExponentialHistogramPointData#getBase()} is 2.0. Then, if <code>offset</code> is 0, the bucket
 * lower bounds would be 1.0, 2.0, 4.0, 8.0, etc. If <code>offset</code> is -3, the bucket lower
 * bounds would be 0.125, 0.25, 0.5, 1.0, 2,0, etc. If <code>offset</code> is +3, the bucket lower
 * bounds would be 8.0, 16.0, 32.0, etc.
 */
@Immutable
public interface ExponentialHistogramBuckets {

  /**
   * The offset shifts the bucket boundaries according to <code>lower_bound = base^(offset+i).
   * </code>.
   *
   * @return the offset.
   */
  int getOffset();

  /**
   * The bucket count is the number of measurements that fall into a bucket corresponding to the
   * given index. Index can be negative, in which case the bucket would be lower bounded somewhere
   * between 0 and 1 (or 0 and -1 if these buckets represent negative measurements).
   *
   * <p>Bucket boundaries are inclusive lower bound and exclusive upper bound.
   *
   * @param index signed int corresponding to the relevant bucket.
   * @return the number of measurements in the bucket.
   */
  long getBucketCountAt(int index);

  /**
   * The total count is the sum of all the values in the buckets.
   *
   * @return the total count.
   */
  long getTotalCount();
}
