/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;

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

  /**
   * Returns the lower bound of a bucket (all values would have been greater than).
   *
   * @param bucketIndex The bucket index, should match {@link #getCounts()} index.
   */
  double getBucketLowerBound(int bucketIndex);

  /**
   * Returns the upper inclusive bound of a bucket (all values would have been less then or equal).
   *
   * @param bucketIndex The bucket index, should match {@link #getCounts()} index.
   */
  double getBucketUpperBound(int bucketIndex);
}
