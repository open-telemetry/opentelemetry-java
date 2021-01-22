/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.concurrent.Immutable;

/**
 * DoubleHistogramPointData represents an approximate representation of the distribution of
 * measurements.
 */
@Immutable
@AutoValue
public abstract class DoubleHistogramPointData implements PointData {
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
      List<Double> boundaries,
      List<Long> counts) {
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
  public abstract List<Double> getBoundaries();

  /**
   * The counts in each bucket.
   *
   * @return the counts in each bucket.
   */
  public abstract List<Long> getCounts();

  /** Iterates over all the bucket boundaries and counts in this histogram. */
  public void forEach(BiConsumer<? super Double, ? super Long> action) {
    List<Double> boundaries = getBoundaries();
    List<Long> counts = getCounts();
    for (int i = 0; i < boundaries.size(); ++i) {
      action.accept(boundaries.get(i), counts.get(i));
    }
    action.accept(Double.POSITIVE_INFINITY, counts.get(boundaries.size()));
  }
}
