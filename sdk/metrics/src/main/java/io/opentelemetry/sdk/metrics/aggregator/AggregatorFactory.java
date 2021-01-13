/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import javax.annotation.concurrent.Immutable;

/** Factory class for {@link Aggregator}. */
@Immutable
public interface AggregatorFactory {
  /**
   * Returns an {@code AggregationFactory} that calculates sum of recorded measurements.
   *
   * @return an {@code AggregationFactory} that calculates sum of recorded measurements.
   */
  static AggregatorFactory sum() {
    return ImmutableAggregatorFactory.SUM;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates count of recorded measurements (the
   * number of recorded measurements).
   *
   * @return an {@code AggregationFactory} that calculates count of recorded measurements (the
   *     number of recorded * measurements).
   */
  static AggregatorFactory count() {
    return ImmutableAggregatorFactory.COUNT;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates the last value of all recorded
   * measurements.
   *
   * <p>Limitation: The current implementation does not store a time when the value was recorded, so
   * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
   * problem because LastValueAggregator is currently only available for Observers which record all
   * values once.
   *
   * @return an {@code AggregationFactory} that calculates the last value of all recorded
   *     measurements.
   */
  static AggregatorFactory lastValue() {
    return ImmutableAggregatorFactory.LAST_VALUE;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates a simple summary of all recorded
   * measurements. The summary consists of the count of measurements, the sum of all measurements,
   * the maximum value recorded and the minimum value recorded.
   *
   * @return an {@code AggregationFactory} that calculates a simple summary of all recorded
   *     measurements.
   */
  static AggregatorFactory minMaxSumCount() {
    return ImmutableAggregatorFactory.MIN_MAX_SUM_COUNT;
  }

  /**
   * Returns a new {@link Aggregator}.
   *
   * @param descriptor the descriptor of the {@code Instrument} that will record values.
   * @return a new {@link Aggregator}.
   */
  <T> Aggregator<T> create(InstrumentDescriptor descriptor);
}
