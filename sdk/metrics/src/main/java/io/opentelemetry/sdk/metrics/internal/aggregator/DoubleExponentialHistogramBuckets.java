/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.state.WindowedCounterArray;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class DoubleExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  public static final int MAX_BUCKETS = 320;

  private final WindowedCounterArray countsArray;
  private final BucketMapper bucketMapper;

  DoubleExponentialHistogramBuckets(int scale) {
    this.countsArray = new WindowedCounterArray(MAX_BUCKETS);
    this.bucketMapper = new LogarithmMapper(scale);
  }

  public void record(double value) {
    int index = bucketMapper.valueToIndex(Math.abs(value));
    this.countsArray.increment(index, 1); // todo handle recording failure
  }

  @Override
  public int getOffset() {
    return (int) countsArray.getIndexStart();
  }

  @Nonnull
  @Override
  public List<Long> getBucketCounts() {
    // todo LongList optimisation
    List<Long> countList =
        new ArrayList<>((int) (countsArray.getIndexEnd() - countsArray.getIndexStart() + 1));
    for (int i = 0; i <= countsArray.getIndexEnd() - countsArray.getIndexStart(); i++) {
      countList.add(i, countsArray.get(i + countsArray.getIndexStart()));
    }
    return countList;
  }

  @Override
  public long getTotalCount() {
    long totalCount = 0;
    for (long i = countsArray.getIndexStart(); i <= countsArray.getIndexEnd(); i++) {
      totalCount += countsArray.get(i);
    }
    return totalCount;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof DoubleExponentialHistogramBuckets)) {
      return false;
    }
    final ExponentialHistogramBuckets other = (ExponentialHistogramBuckets) obj;
    // Don't need to compare getTotalCount() because equivalent bucket counts
    // imply equivalent overall count.
    return getBucketCounts().equals(other.getBucketCounts()) && getOffset() == other.getOffset();
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= 1000003;
    hash ^= getOffset();
    hash *= 1000003;
    hash ^= getBucketCounts().hashCode();
    // Don't need to hash getTotalCount() because equivalent bucket
    // counts imply equivalent overall count.
    return hash;
  }

  private static class LogarithmMapper implements BucketMapper {

    private final double scaleFactor;

    LogarithmMapper(int scale) {
      this.scaleFactor = Math.scalb(1D / Math.log(2), scale);
    }

    @Override
    public int valueToIndex(double value) {
      return (int) Math.floor(Math.log(value) * scaleFactor);
    }
  }
}
