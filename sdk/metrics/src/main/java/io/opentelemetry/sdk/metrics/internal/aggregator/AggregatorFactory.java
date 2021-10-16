/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.concurrent.Immutable;

/**
 * Factory class for {@link Aggregator}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public interface AggregatorFactory {
  /**
   * Returns an {@code AggregationFactory} that calculates sum of recorded measurements.
   *
   * <p>This factory produces {@link Aggregator} that will always produce Sum metrics, the
   * monotonicity is determined based on the instrument type (for Counter and SumObserver will be
   * monotonic, otherwise not).
   *
   * @return an {@code AggregationFactory} that calculates sum of recorded measurements.
   */
  static AggregatorFactory sum() {
    return SumAggregatorFactory.INSTANCE;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates count of recorded measurements (the
   * number of recorded measurements).
   *
   * <p>This factory produces {@link Aggregator} that will always produce monotonic Sum metrics
   * independent of the instrument type. The sum represents the number of measurements recorded.
   *
   * @return an {@code AggregationFactory} that calculates count of recorded measurements (the
   *     number of recorded * measurements).
   */
  static AggregatorFactory count() {
    return CountAggregatorFactory.INSTANCE;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates the last value of all recorded
   * measurements.
   *
   * <p>This factory produces {@link Aggregator} that will always produce gauge metrics independent
   * of the instrument type.
   *
   * <p>Limitation: The current implementation does not store a time when the value was recorded, so
   * merging multiple LastValueAggregators will not preserve the ordering of records.
   *
   * @return an {@code AggregationFactory} that calculates the last value of all recorded
   *     measurements.
   */
  static AggregatorFactory lastValue() {
    return LastValueAggregatorFactory.INSTANCE;
  }

  /**
   * Returns an {@code AggregationFactory} that calculates a simple summary of all recorded
   * measurements. The summary consists of the count of measurements, the sum of all measurements,
   * the maximum value recorded and the minimum value recorded.
   *
   * <p>This factory produces {@link Aggregator} that will always produce double summary metrics
   * independent of the instrument type.
   *
   * @return an {@code AggregationFactory} that calculates a simple summary of all recorded
   *     measurements.
   */
  static AggregatorFactory minMaxSumCount() {
    return MinMaxSumCountAggregatorFactory.INSTANCE;
  }

  /**
   * Returns an {@code AggregatorFactory} that calculates an approximation of the distribution of
   * the measurements taken.
   *
   * @param boundaries configures the fixed bucket boundaries.
   * @return an {@code AggregationFactory} that calculates histogram of recorded measurements.
   * @since 1.1.0
   */
  static AggregatorFactory histogram(List<Double> boundaries) {
    return new HistogramAggregatorFactory(boundaries);
  }

  /**
   * Returns a new {@link Aggregator}.
   *
   * @param instrumentDescriptor the descriptor of the {@code Instrument} that will record
   *     measurements.
   * @param reservoirFactory the constructor of exemplar reservoirs.
   * @return a new {@link Aggregator}.
   */
  <T> Aggregator<T> create(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir> reservoirFactory);
}
