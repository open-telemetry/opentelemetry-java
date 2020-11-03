/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Aggregator represents the interface for all the available aggregations. */
@ThreadSafe
public interface Aggregator {

  /**
   * Merges the current value into the given {@code aggregator} and resets the current value in this
   * {@code Aggregator}.
   *
   * @param aggregator value to merge into.
   */
  void mergeToAndReset(Aggregator aggregator);

  /**
   * Returns the {@code Point} with the given properties and the value from this Aggregation.
   *
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @param labels the labels for the {@code Point}.
   * @return the {@code Point} with the value from this Aggregation.
   */
  @Nullable
  Point toPoint(long startEpochNanos, long epochNanos, Labels labels);

  /**
   * Updates the current aggregator with a newly recorded {@code long} value.
   *
   * @param value the new {@code long} value to be added.
   */
  void recordLong(long value);

  /**
   * Updates the current aggregator with a newly recorded {@code double} value.
   *
   * @param value the new {@code double} value to be added.
   */
  void recordDouble(double value);

  /** Whether there have been any recordings since this aggregator has been reset. */
  boolean hasRecordings();
}
