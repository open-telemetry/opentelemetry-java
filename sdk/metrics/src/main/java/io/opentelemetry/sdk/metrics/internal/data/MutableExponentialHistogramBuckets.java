/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.List;
import java.util.Objects;

/**
 * A mutable {@link ExponentialHistogramBuckets}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public final class MutableExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  private int scale;
  private int offset;
  private long totalCount;
  private DynamicPrimitiveLongList bucketCounts = DynamicPrimitiveLongList.empty();

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
      int scale, int offset, long totalCount, DynamicPrimitiveLongList bucketCounts) {
    this.scale = scale;
    this.offset = offset;
    this.totalCount = totalCount;
    this.bucketCounts = bucketCounts;

    return this;
  }

  @Override
  public String toString() {
    return "MutableExponentialHistogramBuckets{"
        + "scale="
        + scale
        + ", "
        + "offset="
        + offset
        + ", "
        + "bucketCounts="
        + bucketCounts
        + ", "
        + "totalCount="
        + totalCount
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExponentialHistogramBuckets) {
      ExponentialHistogramBuckets that = (ExponentialHistogramBuckets) o;
      return this.scale == that.getScale()
          && this.offset == that.getOffset()
          && this.totalCount == that.getTotalCount()
          && Objects.equals(this.bucketCounts, that.getBucketCounts());
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
