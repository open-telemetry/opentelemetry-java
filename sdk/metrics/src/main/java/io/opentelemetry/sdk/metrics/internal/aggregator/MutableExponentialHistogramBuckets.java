/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.List;
import java.util.Objects;

public class MutableExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  private int scale;
  private int offset;
  private long totalCount;
  private DynamicPrimitiveLongList bucketCounts = new DynamicPrimitiveLongList();

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
    return bucketCounts;
  }

  public DynamicPrimitiveLongList getReusableBucketCountsList() {
    return bucketCounts;
  }

  @Override
  public long getTotalCount() {
    return totalCount;
  }

  public MutableExponentialHistogramBuckets set(
      int scale,
      int offset,
      long totalCount,
      DynamicPrimitiveLongList bucketCounts) {
    this.scale = scale;
    this.offset = offset;
    this.totalCount = totalCount;
    this.bucketCounts = bucketCounts;

    return this;
  }

  @Override
  public String toString() {
    return "MutableExponentialHistogramBuckets{"
        + "scale=" + scale + ", "
        + "offset=" + offset + ", "
        + "bucketCounts=" + bucketCounts + ", "
        + "totalCount=" + totalCount
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MutableExponentialHistogramBuckets) {
      MutableExponentialHistogramBuckets that = (MutableExponentialHistogramBuckets) o;
      return this.scale == that.getScale()
          && this.offset == that.getOffset()
          && this.totalCount == that.getTotalCount()
          && Objects.equals(this.bucketCounts, that.bucketCounts);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = scale;
    result = 31 * result + offset;
    result = 31 * result + (int) (totalCount ^ (totalCount >>> 32));
    result = 31 * result + (bucketCounts != null ? bucketCounts.hashCode() : 0);
    return result;
  }
}
