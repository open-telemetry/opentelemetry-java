/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.concurrent.Immutable;

/** Factory class for {@link Aggregator}. */
@Immutable
public interface AggregationFactory {
  /**
   * Returns an {@code AggregationFactory} that calculates sum of recorded measurements.
   *
   * @return an {@code AggregationFactory} that calculates sum of recorded measurements.
   */
  static AggregationFactory sum() {
    return ImmutableAggregationFactory.SUM;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates count of recorded measurements (the
   * number of recorded measurements).
   *
   * @return an {@code AggregationFactory} that calculates count of recorded measurements (the
   *     number of recorded * measurements).
   */
  static AggregationFactory count() {
    return ImmutableAggregationFactory.COUNT;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates the last value of all recorded
   * measurements.
   *
   * @return an {@code AggregationFactory} that calculates the last value of all recorded
   *     measurements.
   */
  static AggregationFactory lastValue() {
    return ImmutableAggregationFactory.LAST_VALUE;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates a simple summary of all recorded
   * measurements. The summary consists of the count of measurements, the sum of all measurements,
   * the maximum value recorded and the minimum value recorded.
   *
   * @return an {@code AggregationFactory} that calculates a simple summary of all recorded
   *     measurements.
   */
  static AggregationFactory minMaxSumCount() {
    return ImmutableAggregationFactory.MIN_MAX_SUM_COUNT;
  }

  /**
   * Returns a new {@link Aggregation}.
   *
   * @param instrumentValueType the type of recorded values for the {@code Instrument}.
   * @return a new {@link Aggregation}.
   */
  <T extends Accumulation> Aggregation<T> create(InstrumentValueType instrumentValueType);
}
