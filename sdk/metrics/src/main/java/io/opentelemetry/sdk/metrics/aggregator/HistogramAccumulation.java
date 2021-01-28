/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.common.ImmutableDoubleArray;
import io.opentelemetry.sdk.metrics.common.ImmutableLongArray;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class HistogramAccumulation {
  /**
   * Creates a new {@link HistogramAccumulation} with the given values.
   *
   * @return a new {@link HistogramAccumulation} with the given values.
   */
  static HistogramAccumulation create(
      long count, double sum, ImmutableDoubleArray boundaries, ImmutableLongArray counts) {
    return new AutoValue_HistogramAccumulation(count, sum, boundaries, counts);
  }

  HistogramAccumulation() {}

  /**
   * The number of measurements taken.
   *
   * @return the count of recorded measurements.
   */
  abstract long getCount();

  /**
   * The sum of all measurements recorded.
   *
   * @return the sum of recorded measurements.
   */
  abstract double getSum();

  /**
   * The bucket boundaries. For a Histogram with N defined boundaries, e.g, [x, y, z]. There are N+1
   * counts: [-inf, x), [x, y), [y, z), [z, +inf].
   *
   * @return the bucket boundaries in increasing order.
   */
  abstract ImmutableDoubleArray getBoundaries();

  /**
   * The counts in each bucket.
   *
   * @return the counts in each bucket.
   */
  abstract ImmutableLongArray getCounts();

  final DoubleHistogramPointData toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return DoubleHistogramPointData.create(
        startEpochNanos, epochNanos, labels, getSum(), getCount(), getBoundaries(), getCounts());
  }
}
