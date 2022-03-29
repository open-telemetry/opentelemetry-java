/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;
import javax.annotation.Nullable;

/**
 * A histogram metric point.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#histogram
 */
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

  /**
   * The min of all measurements recorded.
   *
   * @return the min of recorded measurements, or {@code null} if not available.
   */
  @Nullable
  Double getMin();

  /**
   * The max of all measurements recorded.
   *
   * @return the max of recorded measurements, or {@code null} if not available.
   */
  @Nullable
  Double getMax();

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
}
