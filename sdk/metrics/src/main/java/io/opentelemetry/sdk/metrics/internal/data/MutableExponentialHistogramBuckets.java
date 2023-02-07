/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.aggregator.AdaptingCircularBufferCounter;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleBase2ExponentialHistogramBuckets;
import java.util.List;

public class MutableExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  private int scale;
  private int offset;
  private final long[] bucketCounts;
  private final List<Long> bucketCountsList;
  private long totalCount;

  public MutableExponentialHistogramBuckets(int maxBuckets) {
    this.bucketCounts = new long[maxBuckets];
    this.bucketCountsList = PrimitiveLongList.wrap(bucketCounts);
  }

  @Override
  public int getScale() {
    return scale;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public List<Long> getBucketCounts() {
    return bucketCountsList;
  }

  @Override
  public long getTotalCount() {
    return totalCount;
  }

  /** Set the values. */
  public void set(DoubleBase2ExponentialHistogramBuckets exponentialHistogramBuckets) {
    this.scale = exponentialHistogramBuckets.getScale();
    this.offset = exponentialHistogramBuckets.getOffset();
    AdaptingCircularBufferCounter counts = exponentialHistogramBuckets.getCounts();
    for (int i = 0; i < bucketCounts.length; i++) {
      this.bucketCounts[i] = counts.get(i + counts.getIndexStart());
    }
    PrimitiveLongList.setSize(
        this.bucketCountsList, counts.getIndexEnd() - counts.getIndexStart() + 1);
    this.totalCount = exponentialHistogramBuckets.getTotalCount();
  }
}
