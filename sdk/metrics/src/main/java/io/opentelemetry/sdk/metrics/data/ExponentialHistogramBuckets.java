/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * ExponentialHistogramBuckets represents either the positive or negative measurements taken for a
 * {@link ExponentialHistogramPointData}.
 *
 * <p>The bucket boundaries are lower-bound inclusive, and are calculated using the {@link
 * ExponentialHistogramPointData#getScale()} and the {@link #getOffset()}.
 *
 * <p>For example, assume {@link ExponentialHistogramPointData#getScale()} is 0, the base is 2.0.
 * Then, if <code>offset</code> is 0, the bucket lower bounds would be 1.0, 2.0, 4.0, 8.0, etc. If
 * <code>offset</code> is -3, the bucket lower bounds would be 0.125, 0.25, 0.5, 1.0, 2,0, etc. If
 * <code>offset</code> is +3, the bucket lower bounds would be 8.0, 16.0, 32.0, etc.
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
   * The bucket counts is a of counts representing number of measurements that fall into each
   * bucket.
   *
   * @return the bucket counts.
   */
  List<Long> getBucketCounts();

  /**
   * The total count is the sum of all the values in the buckets.
   *
   * @return the total count.
   */
  long getTotalCount();
}
