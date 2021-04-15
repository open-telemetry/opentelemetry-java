/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/** Factory class for {@link Aggregator}. */
@Immutable
public interface AggregatorFactory {
  /**
   * Returns an {@code AggregationFactory} that calculates sum of recorded measurements.
   *
   * <p>This factory produces {@link Aggregator} that will always produce Sum metrics, the
   * monotonicity is determined based on the instrument type (for Counter and SumObserver will be
   * monotonic, otherwise not).
   *
   * @param alwaysCumulative configures to always produce {@link AggregationTemporality#CUMULATIVE}
   *     if {@code true} OR {@link AggregationTemporality#DELTA} for all types except SumObserver
   *     and UpDownSumObserver which will always produce {@link AggregationTemporality#CUMULATIVE}.
   * @return an {@code AggregationFactory} that calculates sum of recorded measurements.
   * @deprecated Use {@link AggregatorFactory#sum(AggregationTemporality)}
   */
  @Deprecated
  static AggregatorFactory sum(boolean alwaysCumulative) {
    return new SumAggregatorFactory(
        alwaysCumulative ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA);
  }

  /**
   * Returns an {@code AggregationFactory} that calculates sum of recorded measurements.
   *
   * <p>This factory produces {@link Aggregator} that will always produce Sum metrics, the
   * monotonicity is determined based on the instrument type (for Counter and SumObserver will be
   * monotonic, otherwise not).
   *
   * @param temporality configures what temporality to be produced for the Sum metrics.
   * @return an {@code AggregationFactory} that calculates sum of recorded measurements.
   */
  static AggregatorFactory sum(AggregationTemporality temporality) {
    return new SumAggregatorFactory(temporality);
  }

  /**
   * Returns an {@code AggregationFactory} that calculates count of recorded measurements (the
   * number of recorded measurements).
   *
   * <p>This factory produces {@link Aggregator} that will always produce monotonic Sum metrics
   * independent of the instrument type. The sum represents the number of measurements recorded.
   *
   * @param temporality configures what temporality to be produced for the Sum metrics.
   * @return an {@code AggregationFactory} that calculates count of recorded measurements (the
   *     number of recorded * measurements).
   */
  static AggregatorFactory count(AggregationTemporality temporality) {
    return new CountAggregatorFactory(temporality);
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
   * @param temporality configures what temporality to be produced for the Histogram metrics.
   * @param boundaries configures the fixed bucket boundaries.
   * @return an {@code AggregationFactory} that calculates histogram of recorded measurements.
   * @since 1.1.0
   */
  static AggregatorFactory histogram(List<Double> boundaries, AggregationTemporality temporality) {
    return new HistogramAggregatorFactory(boundaries, temporality);
  }

  /**
   * Returns a new {@link Aggregator}.
   *
   * @param resource the Resource associated with the {@code Instrument} that will record
   *     measurements.
   * @param instrumentationLibraryInfo the InstrumentationLibraryInfo associated with the {@code
   *     Instrument} that will record measurements.
   * @param descriptor the descriptor of the {@code Instrument} that will record measurements.
   * @return a new {@link Aggregator}.
   */
  <T> Aggregator<T> create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor);
}
