/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Aggregator represents the interface for all the available aggregations. */
@ThreadSafe
public interface Aggregator {

  /**
   * Returns the current value into as {@link Accumulation} and resets the current value in this
   * {@code Aggregator}.
   */
  @Nullable
  Accumulation accumulateThenReset();

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
}
