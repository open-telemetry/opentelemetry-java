/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounter;
import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;
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

  private static final double LOG_BASE2_E = 1D / Math.log(2);

  private final ExponentialCounterFactory counterFactory;
  private ExponentialCounter counts;
  private int scale;
  private double scaleFactor;
  private long totalCount;

  DoubleExponentialHistogramBuckets(
      int scale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    this.counterFactory = counterFactory;
    this.counts = counterFactory.newCounter(maxBuckets);
    this.scale = scale;
    this.scaleFactor = computeScaleFactor(scale);
    this.totalCount = 0;
  }

  // For copying
  DoubleExponentialHistogramBuckets(DoubleExponentialHistogramBuckets buckets) {
    this.counterFactory = buckets.counterFactory;
    this.counts = counterFactory.copy(buckets.counts);
    this.scale = buckets.scale;
    this.scaleFactor = buckets.scaleFactor;
    this.totalCount = buckets.totalCount;
  }

  /** Returns a copy of this bucket. */
  DoubleExponentialHistogramBuckets copy() {
    return new DoubleExponentialHistogramBuckets(this);
  }

  /** Resets all counters in this bucket set to zero, but preserves scale. */
  public void clear() {
    this.totalCount = 0;
    this.counts.clear();
  }

  boolean record(double value) {
    if (value == 0.0) {
      // Guarded by caller. If passed 0 it would be a bug in the SDK.
      throw new IllegalStateException("Illegal attempted recording of zero at bucket level.");
    }
    int index = valueToIndex(value);
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

  @Nonnull
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
      ExponentialCounter newCounts = counterFactory.copy(counts);
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
    this.scaleFactor = computeScaleFactor(this.scale);
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
    DoubleExponentialHistogramBuckets copy = a.copy();
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
      return a;
    } else if (a.counts.isEmpty()) {
      return b;
    }
    DoubleExponentialHistogramBuckets copy = a.copy();
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
    this.totalCount += sign * other.totalCount;
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
    long index = valueToIndex(value);
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

  private int getIndexByLogarithm(double value) {
    return (int) Math.floor(Math.log(value) * scaleFactor);
  }

  private int getIndexByExponent(double value) {
    return Math.getExponent(value) >> -scale;
  }

  private static double computeScaleFactor(int scale) {
    return Math.scalb(LOG_BASE2_E, scale);
  }

  /**
   * Maps a recorded double value to a bucket index.
   *
   * <p>The strategy to retrieve the index is specified in the OpenTelemetry specification:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponential-buckets
   *
   * @param value Measured value (must be non-zero).
   * @return the index of the bucket which the value maps to.
   */
  private int valueToIndex(double value) {
    double absValue = Math.abs(value);
    if (scale > 0) {
      return getIndexByLogarithm(absValue);
    }
    return getIndexByExponent(absValue);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof DoubleExponentialHistogramBuckets)) {
      return false;
    }
    DoubleExponentialHistogramBuckets other = (DoubleExponentialHistogramBuckets) obj;
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
  private boolean sameBucketCounts(DoubleExponentialHistogramBuckets other) {
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
