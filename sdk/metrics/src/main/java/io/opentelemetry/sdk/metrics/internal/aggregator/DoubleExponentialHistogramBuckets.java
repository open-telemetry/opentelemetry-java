/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounter;
import io.opentelemetry.sdk.metrics.internal.state.MapCounter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class DoubleExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  public static final int MAX_BUCKETS = 320;
  public static final int MAX_SCALE = 20;

  private static final Logger logger = Logger.getLogger(DoubleExponentialHistogramBuckets.class.getName());

  private ExponentialCounter counts;
  private BucketMapper bucketMapper;
  private int scale;

  DoubleExponentialHistogramBuckets() {
    this.counts = new MapCounter(MAX_BUCKETS);
    this.bucketMapper = new LogarithmMapper(MAX_SCALE);
    this.scale = MAX_SCALE;
  }

  public boolean record(double value) {
    long index = bucketMapper.valueToIndex(Math.abs(value));
    return this.counts.increment(index, 1);
  }

  @Override
  public int getOffset() {
    return (int) counts.getIndexStart();
  }

  @Nonnull
  @Override
  public List<Long> getBucketCounts() {
    // todo LongList optimisation
    int length = (int) (counts.getIndexEnd() - counts.getIndexStart() + 1);
    Long[] countsArr = new Long[(int) (counts.getIndexEnd() - counts.getIndexStart() + 1)];
    for (int i = 0; i < length; i++) {
      countsArr[i] = counts.get(i + counts.getIndexStart());
    }
    return Arrays.asList(countsArr);
  }

  @Override
  public long getTotalCount() {
    long totalCount = 0;
    for (long i = counts.getIndexStart(); i <= counts.getIndexEnd(); i++) {
      totalCount += counts.get(i);
    }
    return totalCount;
  }

  void downscale(int by) {
    if (by <= 0) {
      logger.warning(
          String.format("downScale() expects positive integer but was given %d. "
                  + "Cannot upscale exponential histogram.", by));
      return;
    }

    if (!counts.isEmpty()) {
      ExponentialCounter newCounts = new MapCounter(MAX_BUCKETS);

      for (long i = counts.getIndexStart(); i <= counts.getIndexEnd(); i++) {
        if (!newCounts.increment(i >> by, counts.get(i))) {
          throw new RuntimeException("Failed to create new downscaled buckets.");
        }
      }
      this.counts = newCounts;
    }

    this.scale = this.scale - by;
    this.bucketMapper = new LogarithmMapper(scale);
  }

  /**
   * Returns the minimum scale reduction required to record the given value in these buckets.
   *
   * @param value The proposed value to be recorded.
   * @return The required scale reduction in order to fit the value in these buckets.
   */
  int getScaleReduction(double value) {
    long index = bucketMapper.valueToIndex(value);
    long newStart = Math.min(index, counts.getIndexStart());
    long newEnd = Math.max(index, counts.getIndexEnd());
    int scaleReduction = 0;

    while (newEnd - newStart + 1 > MAX_BUCKETS) {
      newStart >>= 1;
      newEnd >>= 1;
      scaleReduction++;
    }
    return scaleReduction;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof DoubleExponentialHistogramBuckets)) {
      return false;
    }
    final DoubleExponentialHistogramBuckets other = (DoubleExponentialHistogramBuckets) obj;
    // Don't need to compare getTotalCount() because equivalent bucket counts
    // imply equivalent overall count.
    return getBucketCounts().equals(other.getBucketCounts())
        && this.getOffset() == other.getOffset()
        && this.scale == other.scale;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= 1000003;
    hash ^= getOffset();
    hash *= 1000003;
    hash ^= getBucketCounts().hashCode();
    hash *= 1000003;
    hash ^= scale;
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
    public long valueToIndex(double value) {
      return (long) Math.floor(Math.log(value) * scaleFactor);
    }
  }
}
