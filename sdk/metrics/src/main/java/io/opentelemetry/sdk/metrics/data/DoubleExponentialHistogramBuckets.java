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
 */
@AutoValue
@Immutable
public abstract class DoubleExponentialHistogramBuckets {
  DoubleExponentialHistogramBuckets() {}

  /**
   * Create DoubleExponentialHistogramBuckets.
   *
   * @param offset Signed integer representing the bucket index of the first entry in bucketCounts.
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

  public abstract int getOffset();

  public abstract List<Long> getBucketCounts();

  public abstract long getTotalCount();
}
