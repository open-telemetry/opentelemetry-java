/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import io.opentelemetry.sdk.metrics.aggregation.MinMaxSumCountAccumulation;
import javax.annotation.concurrent.Immutable;

/**
 * Aggregator represents the abstract class for all the available aggregations that can be computed
 * during the accumulation phase for all the instrument.
 *
 * <p>The synchronous instruments will create an {@link AggregatorHandle} to record individual
 * measurements synchronously, and for asynchronous the {@link #accumulateDouble(double)} or {@link
 * #accumulateLong(long)} will be used when reading values from the instrument callbacks.
 */
@Immutable
public interface Aggregator<T extends Accumulation> {
  /**
   * Returns a count {@link Aggregator}.
   *
   * @return a count {@link Aggregator}.
   */
  static Aggregator<LongAccumulation> count() {
    return CountAggregator.INSTANCE;
  }

  /**
   * Returns a last value {@link Aggregator} for {@code double} measurements.
   *
   * <p>Limitation: The current implementation does not store a time when the value was recorded, so
   * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
   * problem because LastValueAggregator is currently only available for Observers which record all
   * values once.
   *
   * @return a last value {@link Aggregator} for {@code double} measurements.
   */
  static Aggregator<DoubleAccumulation> doubleLastValue() {
    return DoubleLastValueAggregator.INSTANCE;
  }

  /**
   * Returns a min, max, sum, count {@link Aggregator} for {@code double} measurements.
   *
   * @return a min, max, sum, count {@link Aggregator} for {@code double} measurements.
   */
  static Aggregator<MinMaxSumCountAccumulation> doubleMinMaxSumCount() {
    return DoubleMinMaxSumCountAggregator.INSTANCE;
  }

  /**
   * Returns a sum {@link Aggregator} for {@code double} measurements.
   *
   * @return a sum {@link Aggregator} for {@code double} measurements.
   */
  static Aggregator<DoubleAccumulation> doubleSum() {
    return DoubleSumAggregator.INSTANCE;
  }

  /**
   * Returns a last value {@link Aggregator} for {@code long} measurements.
   *
   * <p>Limitation: The current implementation does not store a time when the value was recorded, so
   * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
   * problem because LastValueAggregator is currently only available for Observers which record all
   * values once.
   *
   * @return a last value {@link Aggregator} for {@code long} measurements.
   */
  static Aggregator<LongAccumulation> longLastValue() {
    return LongLastValueAggregator.INSTANCE;
  }

  /**
   * Returns a min, max, sum, count {@link Aggregator} for {@code long} measurements.
   *
   * @return a min, max, sum, count {@link Aggregator} for {@code long} measurements.
   */
  static Aggregator<MinMaxSumCountAccumulation> longMinMaxSumCount() {
    return LongMinMaxSumCountAggregator.INSTANCE;
  }

  /**
   * Returns a sum {@link Aggregator} for {@code long} measurements.
   *
   * @return a sum {@link Aggregator} for {@code long} measurements.
   */
  static Aggregator<LongAccumulation> longSum() {
    return LongSumAggregator.INSTANCE;
  }

  /**
   * Returns a new {@link AggregatorHandle}. This MUST by used by the synchronous to aggregate
   * recorded measurements during the collection cycle.
   *
   * @return a new {@link AggregatorHandle}.
   */
  AggregatorHandle<T> createHandle();

  /**
   * Returns a new {@code Accumulation} for the given value. This MUST be used by the asynchronous
   * instruments to create {@code Accumulation} that are passed to the processor.
   *
   * @param value the given value to be used to create the {@code Accumulation}.
   * @return a new {@code Accumulation} for the given value.
   */
  default T accumulateLong(long value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording long values.");
  }

  /**
   * Returns a new {@code Accumulation} for the given value. This MUST be used by the asynchronous
   * instruments to create {@code Accumulation} that are passed to the processor.
   *
   * @param value the given value to be used to create the {@code Accumulation}.
   * @return a new {@code Accumulation} for the given value.
   */
  default T accumulateDouble(double value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording double values.");
  }
}
