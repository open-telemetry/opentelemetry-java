/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * DoubleExponentialHistogramBuckets represents either the positive or negative measurements taken
 * for a {@link DoubleExponentialHistogramPointData}.
 *
 * <p>The bucket boundaries are lower-bound inclusive, and are calculated using the {@link
 * DoubleExponentialHistogramPointData#getScale()} and the {@link #getOffset()}.
 *
 * <p>For example, assume {@link DoubleExponentialHistogramPointData#getScale()} is 0, implying
 * {@link DoubleExponentialHistogramPointData#getBase()} is 2.0. Then, if <code>offset</code> is 0,
 * the bucket lower bounds would be 1.0, 2.0, 4.0, 8.0, etc. If <code>offset</code> is -3, the
 * bucket lower bounds would be 0.125, 0.25, 0.5, 1.0, 2,0, etc. If <code>offset</code> is +3, the
 * bucket lower bounds would be 8.0, 16.0, 32.0, etc.
 */
@AutoValue
@Immutable
public abstract class DoubleExponentialHistogramBuckets {
  DoubleExponentialHistogramBuckets() {}

  /**
   * Create DoubleExponentialHistogramBuckets.
   *
   * @param offset Signed integer which shifts the bucket boundaries according to <code>
   *     lower_bound = base^(offset+i).</code>
   * @param bucketCounts List of counts representing number of measurements that fall into each
   *     bucket.
   * @return a DoubleExponentialHistogramBuckets.
   */
  public DoubleExponentialHistogramBuckets create(int offset, List<Long> bucketCounts) {
    long totalCount = 0;
    for (long count : bucketCounts) {
      totalCount += count;
    }
    return new AutoValue_DoubleExponentialHistogramBuckets(
        offset, Collections.unmodifiableList(new ArrayList<>(bucketCounts)), totalCount);
  }

  /**
   * The offset shifts the bucket boundaries according to <code>lower_bound = base^(offset+i).
   * </code>.
   *
   * @return the offset.
   */
  public abstract int getOffset();

  /**
   * The bucket counts is a of counts representing number of measurements that fall into each
   * bucket.
   *
   * @return the bucket counts.
   */
  public abstract List<Long> getBucketCounts();

  /**
   * The total count is the sum of all the values in the list {@link #getBucketCounts()}.
   *
   * @return the total count.
   */
  public abstract long getTotalCount();
}
