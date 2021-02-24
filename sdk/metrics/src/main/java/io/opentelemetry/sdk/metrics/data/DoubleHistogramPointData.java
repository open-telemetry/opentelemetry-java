/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.common.Labels;
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
      List<Double> boundaries,
      List<Long> counts) {
    return new AutoValue_DoubleHistogramPointData(
        startEpochNanos,
        epochNanos,
        labels,
        sum,
        count,
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
   * counts: [-inf, x), [x, y), [y, z), [z, +inf].
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

  /** Iterates over all the bucket boundaries and counts in this histogram. */
  public void forEach(BucketConsumer action) {
    List<Double> boundaries = getBoundaries();
    List<Long> counts = getCounts();
    for (int i = 0; i < boundaries.size(); ++i) {
      action.accept(boundaries.get(i), counts.get(i));
    }
    action.accept(Double.POSITIVE_INFINITY, counts.get(boundaries.size()));
  }
}
