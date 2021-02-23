/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.metrics.common.ImmutableDoubleArray;
import io.opentelemetry.sdk.metrics.common.ImmutableLongArray;
import javax.annotation.concurrent.Immutable;

/**
 * DoubleHistogramPointData represents an approximate representation of the distribution of
 * measurements.
 */
@Immutable
@AutoValue
public abstract class DoubleHistogramPointData implements PointData {
  /**
   * Functional interface for consuming bucket boundaries and counts as a sequence of pair values.
   */
  public interface BucketConsumer {
    void accept(double upperBound, long count);
  }

  /**
   * Creates a DoubleHistogramPointData.
   *
   * @return a DoubleHistogramPointData.
   */
  public static DoubleHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Labels labels,
      double sum,
      long count,
      ImmutableDoubleArray boundaries,
      ImmutableLongArray counts) {
    return new AutoValue_DoubleHistogramPointData(
        startEpochNanos, epochNanos, labels, sum, count, boundaries, counts);
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
   * counts: [-inf, x), [x, y), [y, z), [z, +inf].
   *
   * @return the bucket boundaries in increasing order.
   */
  public abstract ImmutableDoubleArray getBoundaries();

  /**
   * The counts in each bucket.
   *
   * @return the counts in each bucket.
   */
  public abstract ImmutableLongArray getCounts();

  /** Iterates over all the bucket boundaries and counts in this histogram. */
  public void forEach(BucketConsumer action) {
    ImmutableDoubleArray boundaries = getBoundaries();
    ImmutableLongArray counts = getCounts();
    for (int i = 0; i < boundaries.length(); ++i) {
      action.accept(boundaries.get(i), counts.get(i));
    }
    action.accept(Double.POSITIVE_INFINITY, counts.get(boundaries.length()));
  }
}
