/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * This class handles the operations for recording, scaling, and exposing data related to the base2
 * exponential histogram.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class DoubleBase2ExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  private AdaptingCircularBufferCounter counts;
  private int scale;
  private Base2ExponentialHistogramIndexer base2ExponentialHistogramIndexer;
  private long totalCount;

  DoubleBase2ExponentialHistogramBuckets(int scale, int maxBuckets) {
    this.counts = new AdaptingCircularBufferCounter(maxBuckets);
    this.scale = scale;
    this.base2ExponentialHistogramIndexer = Base2ExponentialHistogramIndexer.get(this.scale);
    this.totalCount = 0;
  }

  // For copying
  DoubleBase2ExponentialHistogramBuckets(DoubleBase2ExponentialHistogramBuckets buckets) {
    this.counts = new AdaptingCircularBufferCounter(buckets.counts);
    this.scale = buckets.scale;
    this.base2ExponentialHistogramIndexer = buckets.base2ExponentialHistogramIndexer;
    this.totalCount = buckets.totalCount;
  }

  /** Returns a copy of this bucket. */
  DoubleBase2ExponentialHistogramBuckets copy() {
    return new DoubleBase2ExponentialHistogramBuckets(this);
  }

  /** Resets all counters in this bucket set to zero, but preserves scale. */
  void clear() {
    this.totalCount = 0;
    this.counts.clear();
  }

  boolean record(double value) {
    if (value == 0.0) {
      // Guarded by caller. If passed 0 it would be a bug in the SDK.
      throw new IllegalStateException("Illegal attempted recording of zero at bucket level.");
    }
    int index = base2ExponentialHistogramIndexer.computeIndex(value);
    boolean recordingSuccessful = this.counts.increment(index, 1);
    if (recordingSuccessful) {
      totalCount++;
    }
    return recordingSuccessful;
  }

  @Override
  public int getOffset() {
    // We need to unify the behavior of empty buckets.
    // Unfortunately, getIndexStart is not meaningful for empty counters, so we default to
    // returning 0 for offset and an empty list.
    if (counts.isEmpty()) {
      return 0;
    }
    return counts.getIndexStart();
  }

  @Override
  public List<Long> getBucketCounts() {
    if (counts.isEmpty()) {
      return Collections.emptyList();
    }
    int length = counts.getIndexEnd() - counts.getIndexStart() + 1;
    long[] countsArr = new long[length];
    for (int i = 0; i < length; i++) {
      countsArr[i] = counts.get(i + counts.getIndexStart());
    }
    return PrimitiveLongList.wrap(countsArr);
  }

  @Override
  public long getTotalCount() {
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
      // We want to preserve other optimisations here as well, e.g. integer size.
      // Instead of  creating a new counter, we copy the existing one (for bucket size
      // optimisations), and clear the values before writing the new ones.
      AdaptingCircularBufferCounter newCounts = new AdaptingCircularBufferCounter(counts);
      newCounts.clear();

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
    this.base2ExponentialHistogramIndexer = Base2ExponentialHistogramIndexer.get(this.scale);
  }

  /**
   * This method merges this instance with another set of buckets. It alters the underlying bucket
   * counts and scale of this instance only, so it is to be used with caution.
   *
   * <p>The bucket counts of this instance will be added to or subtracted from depending on the
   * additive parameter.
   *
   * <p>This algorithm for merging is adapted from NrSketch.
   *
   * @param other the histogram that will be merged into this one
   */
  void mergeInto(DoubleBase2ExponentialHistogramBuckets other) {
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

    // do actual merging of other into this.
    for (int i = other.getOffset(); i <= other.counts.getIndexEnd(); i++) {
      if (!this.counts.increment(i >> deltaOther, other.counts.get(i))) {
        // This should never occur if scales and windows are calculated without bugs
        throw new IllegalStateException("Failed to merge exponential histogram buckets.");
      }
    }
    this.totalCount += other.totalCount;
  }

  @Override
  public int getScale() {
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
    long index = base2ExponentialHistogramIndexer.computeIndex(value);
    long newStart = Math.min(index, counts.getIndexStart());
    long newEnd = Math.max(index, counts.getIndexEnd());
    return getScaleReduction(newStart, newEnd);
  }

  int getScaleReduction(long newStart, long newEnd) {
    int scaleReduction = 0;

    while (newEnd - newStart + 1 > counts.getMaxSize()) {
      newStart >>= 1;
      newEnd >>= 1;
      scaleReduction++;
    }
    return scaleReduction;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof DoubleBase2ExponentialHistogramBuckets)) {
      return false;
    }
    DoubleBase2ExponentialHistogramBuckets other = (DoubleBase2ExponentialHistogramBuckets) obj;
    // Don't need to compare getTotalCount() because equivalent bucket counts
    // imply equivalent overall count.
    // Additionally, we compare the "semantics" of bucket counts, that is
    // it's ok for getOffset() to diverge as long as the populated counts remain
    // the same.  This is because we don't "normalize" buckets after doing
    // difference/subtraction operations.
    return this.scale == other.scale && sameBucketCounts(other);
  }

  /**
   * Tests if two bucket counts are equivalent semantically.
   *
   * <p>Semantic equivalence means:
   *
   * <ul>
   *   <li>All counts are stored between indexStart/indexEnd.
   *   <li>Offset does NOT need to be the same
   * </ul>
   */
  private boolean sameBucketCounts(DoubleBase2ExponentialHistogramBuckets other) {
    if (this.totalCount != other.totalCount) {
      return false;
    }
    int min = Math.min(this.counts.getIndexStart(), other.counts.getIndexStart());

    // This check is so we avoid iterating from Integer.MIN_VALUE.
    // In this case, we can assume that those buckets are empty.
    // We start iterating from the other bucket index instead.
    // They still may be equal as it is possible for another set of buckets
    // to be empty but have a higher start index.
    if (min == Integer.MIN_VALUE) {
      min = Math.max(this.counts.getIndexStart(), other.counts.getIndexStart());
    }

    int max = Math.max(this.counts.getIndexEnd(), other.counts.getIndexEnd());
    for (int idx = min; idx <= max; idx++) {
      if (this.counts.get(idx) != other.counts.get(idx)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= 1000003;
    // We need a new algorithm here that lines up w/ equals, so we only use non-zero counts.
    for (int idx = this.counts.getIndexStart(); idx <= this.counts.getIndexEnd(); idx++) {
      long count = this.counts.get(idx);
      if (count != 0) {
        hash ^= idx;
        hash *= 1000003;
        hash = (int) (hash ^ count);
        hash *= 1000003;
      }
    }
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
}
