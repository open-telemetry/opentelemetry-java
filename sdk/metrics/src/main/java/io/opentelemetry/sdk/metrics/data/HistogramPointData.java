/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Point data for {@link HistogramData}.
 *
 * @since 1.14.0
 */
@Immutable
public interface HistogramPointData extends PointData {
  /**
   * The sum of all measurements recorded.
   *
   * @return the sum of recorded measurements.
   */
  double getSum();

  /**
   * The number of measurements taken.
   *
   * @return the count of recorded measurements.
   */
  long getCount();

  /** Return {@code true} if {@link #getMin()} is set. */
  boolean hasMin();

  /**
   * The min of all measurements recorded, if {@link #hasMin()} is {@code true}. If {@link
   * #hasMin()} is {@code false}, the response should be ignored.
   */
  double getMin();

  /** Return {@code true} if {@link #getMax()} is set. */
  boolean hasMax();

  /**
   * The max of all measurements recorded, if {@link #hasMax()} is {@code true}. If {@link
   * #hasMax()} is {@code false}, the response should be ignored.
   */
  double getMax();

  /**
   * The bucket boundaries. For a Histogram with N defined boundaries, e.g, [x, y, z]. There are N+1
   * counts: (-inf, x], (x, y], (y, z], (z, +inf).
   *
   * @return the read-only bucket boundaries in increasing order. <b>do not mutate</b> the returned
   *     object.
   */
  List<Double> getBoundaries();

  /**
   * The counts in each bucket.
   *
   * @return the read-only counts in each bucket. <b>do not mutate</b> the returned object.
   */
  List<Long> getCounts();

  /** List of exemplars collected from measurements that were used to form the data point. */
  @Override
  List<DoubleExemplarData> getExemplars();
}
