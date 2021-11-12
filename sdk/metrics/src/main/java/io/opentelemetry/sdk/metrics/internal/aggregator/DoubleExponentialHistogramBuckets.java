/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounter;
import io.opentelemetry.sdk.metrics.internal.state.MapCounter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class handles the operations for recording, scaling, and exposing data related to the
 * exponential histogram.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class DoubleExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  public static final int MAX_SCALE = 20;

  private static final int MAX_BUCKETS = MapCounter.MAX_SIZE;

  private ExponentialCounter counts;
  private BucketMapper bucketMapper;
  private int scale;

  DoubleExponentialHistogramBuckets() {
    this.counts = new MapCounter();
    this.bucketMapper = new LogarithmMapper(MAX_SCALE);
    this.scale = MAX_SCALE;
  }

  // For copying
  DoubleExponentialHistogramBuckets(DoubleExponentialHistogramBuckets buckets) {
    this.counts = new MapCounter(buckets.counts); // copy counts
    this.bucketMapper = new LogarithmMapper(buckets.scale);
    this.scale = buckets.scale;
  }

  boolean record(double value) {
    if (value == 0.0) {
      // Guarded by caller. If passed 0 it would be a bug in the SDK.
      throw new IllegalStateException("Illegal attempted recording of zero at bucket level.");
    }
    int index = bucketMapper.valueToIndex(Math.abs(value));
    return this.counts.increment(index, 1);
  }

  @Override
  public int getOffset() {
    return counts.getIndexStart();
  }

  @Nonnull
  @Override
  public List<Long> getBucketCounts() {
    if (counts.isEmpty()) {
      return Collections.emptyList();
    }
    int length = counts.getIndexEnd() - counts.getIndexStart() + 1;
    Long[] countsArr = new Long[length];
    for (int i = 0; i < length; i++) {
      countsArr[i] = counts.get(i + counts.getIndexStart());
    }
    return Arrays.asList(countsArr);
  }

  @Override
  public long getTotalCount() {
    long totalCount = 0;
    for (int i = counts.getIndexStart(); i <= counts.getIndexEnd(); i++) {
      totalCount += counts.get(i);
    }
    return totalCount;
  }

  void downscale(int by) {
    if (by == 0) {
      return;
    } else if (by < 0) {
      // This should never happen without an SDK bug
      throw new IllegalStateException("Cannot downscale by negative amount. Was given " + by + ".");
    }

    if (!counts.isEmpty()) {
      ExponentialCounter newCounts = new MapCounter();

      for (int i = counts.getIndexStart(); i <= counts.getIndexEnd(); i++) {
        long count = counts.get(i);
        if (count > 0) {
          if (!newCounts.increment(i >> by, count)) {
            // Theoretically won't happen unless there's an overflow on index
            throw new IllegalStateException("Failed to create new downscaled buckets.");
          }
        }
      }
      this.counts = newCounts;
    }

    this.scale = this.scale - by;
    this.bucketMapper = new LogarithmMapper(scale);
  }

  /**
   * Return buckets a subtracted by buckets b. May perform downscaling if required.
   *
   * @param a the minuend of the subtraction.
   * @param b the subtrahend of the subtraction.
   * @return buckets a subtracted by buckets b.
   */
  static DoubleExponentialHistogramBuckets diff(
      DoubleExponentialHistogramBuckets a, DoubleExponentialHistogramBuckets b) {
    DoubleExponentialHistogramBuckets copy = new DoubleExponentialHistogramBuckets(a);
    copy.mergeWith(b, /* additive= */ false);
    return copy;
  }

  /**
   * Immutable method for merging. This method copies the first set of buckets, performs the merge
   * on the copy, and returns the copy.
   *
   * @param a first buckets
   * @param b second buckets
   * @return A new set of buckets, the result
   */
  static DoubleExponentialHistogramBuckets merge(
      DoubleExponentialHistogramBuckets a, DoubleExponentialHistogramBuckets b) {
    if (b.counts.isEmpty()) {
      return new DoubleExponentialHistogramBuckets(a);
    } else if (a.counts.isEmpty()) {
      return new DoubleExponentialHistogramBuckets(b);
    }
    DoubleExponentialHistogramBuckets copy = new DoubleExponentialHistogramBuckets(a);
    copy.mergeWith(b, /* additive= */ true);
    return copy;
  }

  /**
   * This method merges this instance with another set of buckets. It alters the underlying bucket
   * counts and scale of this instance only, so it is to be used with caution. For immutability, use
   * the static merge() method.
   *
   * <p>The bucket counts of this instance will be added to or subtracted from depending on the
   * additive parameter.
   *
   * <p>This algorithm for merging is adapted from NrSketch.
   *
   * @param other the histogram that will be merged into this one
   * @param additive whether the bucket counts will be added or subtracted (diff vs merge).
   */
  private void mergeWith(DoubleExponentialHistogramBuckets other, boolean additive) {
    if (other.counts.isEmpty()) {
      return;
    }

    // Find the common scale, and the extended window required to merge the two bucket sets
    int commonScale = Math.min(this.scale, other.scale);

    // Deltas are changes in scale
    int deltaThis = this.scale - commonScale;
    int deltaOther = other.scale - commonScale;

    long newWindowStart;
    long newWindowEnd;
    if (this.counts.isEmpty()) {
      newWindowStart = other.getOffset() >> deltaOther;
      newWindowEnd = other.counts.getIndexEnd() >> deltaOther;
    } else {
      newWindowStart = Math.min(this.getOffset() >> deltaThis, other.getOffset() >> deltaOther);
      newWindowEnd =
          Math.max(
              (this.counts.getIndexEnd() >> deltaThis), (other.counts.getIndexEnd() >> deltaOther));
    }

    // downscale to fit new window
    deltaThis += getScaleReduction(newWindowStart, newWindowEnd);
    this.downscale(deltaThis);

    // since we changed scale of this, we need to know the new difference between the two scales
    deltaOther = other.scale - this.scale;

    // do actual merging of other into this. Will decrement or increment depending on sign.
    int sign = additive ? 1 : -1;
    for (int i = other.getOffset(); i <= other.counts.getIndexEnd(); i++) {
      if (!this.counts.increment(i >> deltaOther, sign * other.counts.get(i))) {
        // This should never occur if scales and windows are calculated without bugs
        throw new IllegalStateException("Failed to merge exponential histogram buckets.");
      }
    }
  }

  int getScale() {
    return scale;
  }

  /**
   * Returns the minimum scale reduction required to record the given value in these buckets, by
   * calculating the new required window to allow the new value to be recorded. To be used with
   * downScale().
   *
   * @param value The proposed value to be recorded.
   * @return The required scale reduction in order to fit the value in these buckets.
   */
  int getScaleReduction(double value) {
    long index = bucketMapper.valueToIndex(Math.abs(value));
    long newStart = Math.min(index, counts.getIndexStart());
    long newEnd = Math.max(index, counts.getIndexEnd());
    return getScaleReduction(newStart, newEnd);
  }

  int getScaleReduction(long newStart, long newEnd) {
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

  @Override
  public String toString() {
    return "DoubleExponentialHistogramBuckets{"
        + "scale: "
        + scale
        + ", offset: "
        + getOffset()
        + ", counts: "
        + counts
        + " }";
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
