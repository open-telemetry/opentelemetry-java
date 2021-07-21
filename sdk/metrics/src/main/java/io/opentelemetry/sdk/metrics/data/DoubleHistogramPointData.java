/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * DoubleHistogramPointData represents an approximate representation of the distribution of
 * measurements.
 */
@Immutable
@AutoValue
public abstract class DoubleHistogramPointData implements PointData {

  /**
   * Creates a DoubleHistogramPointData. For a Histogram with N defined boundaries, there should be
   * N+1 counts.
   *
   * @return a DoubleHistogramPointData.
   * @throws IllegalArgumentException if the given boundaries/counts were invalid
   */
  public static DoubleHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double sum,
      List<Double> boundaries,
      List<Long> counts) {
    return create(
        startEpochNanos, epochNanos, attributes, sum, boundaries, counts, Collections.emptyList());
  }

  /**
   * Creates a DoubleHistogramPointData. For a Histogram with N defined boundaries, there should be
   * N+1 counts.
   *
   * @return a DoubleHistogramPointData.
   * @throws IllegalArgumentException if the given boundaries/counts were invalid
   */
  public static DoubleHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double sum,
      List<Double> boundaries,
      List<Long> counts,
      List<Exemplar> exemplars) {
    if (counts.size() != boundaries.size() + 1) {
      throw new IllegalArgumentException(
          "invalid counts: size should be "
              + (boundaries.size() + 1)
              + " instead of "
              + counts.size());
    }
    if (!isStrictlyIncreasing(boundaries)) {
      throw new IllegalArgumentException("invalid boundaries: " + boundaries);
    }
    if (!boundaries.isEmpty()
        && (boundaries.get(0).isInfinite() || boundaries.get(boundaries.size() - 1).isInfinite())) {
      throw new IllegalArgumentException("invalid boundaries: contains explicit +/-Inf");
    }

    long totalCount = 0;
    for (long c : counts) {
      totalCount += c;
    }
    return new AutoValue_DoubleHistogramPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        exemplars,
        sum,
        totalCount,
        Collections.unmodifiableList(new ArrayList<>(boundaries)),
        Collections.unmodifiableList(new ArrayList<>(counts)));
  }

  DoubleHistogramPointData() {}

  /**
   * The sum of all measurements recorded.
   *
   * @return the sum of recorded measurements.
   */
  public abstract double getSum();

  /**
   * The number of measurements taken.
   *
   * @return the count of recorded measurements.
   */
  public abstract long getCount();

  /**
   * The bucket boundaries. For a Histogram with N defined boundaries, e.g, [x, y, z]. There are N+1
   * counts: (-inf, x], (x, y], (y, z], (z, +inf).
   *
   * @return the read-only bucket boundaries in increasing order. <b>do not mutate</b> the returned
   *     object.
   */
  public abstract List<Double> getBoundaries();

  /**
   * The counts in each bucket.
   *
   * @return the read-only counts in each bucket. <b>do not mutate</b> the returned object.
   */
  public abstract List<Long> getCounts();

  /**
   * Returns the lower bound of a bucket (all values would have been greater than).
   *
   * @param bucketIndex The bucket index, should match {@link #getCounts()} index.
   */
  public double getBucketLowerBound(int bucketIndex) {
    return bucketIndex > 0 ? getBoundaries().get(bucketIndex - 1) : Double.NEGATIVE_INFINITY;
  }
  /**
   * Returns the upper inclusive bound of a bucket (all values would have been less then or equal).
   *
   * @param bucketIndex The bucket index, should match {@link #getCounts()} index.
   */
  public double getBucketUpperBound(int bucketIndex) {
    return (bucketIndex < getBoundaries().size())
        ? getBoundaries().get(bucketIndex)
        : Double.POSITIVE_INFINITY;
  }

  private static boolean isStrictlyIncreasing(List<Double> xs) {
    for (int i = 0; i < xs.size() - 1; i++) {
      if (xs.get(i).compareTo(xs.get(i + 1)) >= 0) {
        return false;
      }
    }
    return true;
  }
}
