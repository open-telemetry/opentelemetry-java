/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Aggregator represents the abstract class that is used for synchronous instruments. It must be
 * thread-safe and avoid locking when possible, because values are recorded synchronously on the
 * calling thread.
 *
 * <p>An {@link AggregatorHandle} must be created for every unique {@link Attributes} recorded.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public abstract class AggregatorHandle<T extends PointData, U extends ExemplarData> {

  // A reservoir of sampled exemplars for this time period.
  private final ExemplarReservoir<U> exemplarReservoir;

  protected AggregatorHandle(ExemplarReservoir<U> exemplarReservoir) {
    this.exemplarReservoir = exemplarReservoir;
  }

  /**
   * Returns the current value into as {@link T}. If {@code reset} is {@code true}, resets the
   * current value in this {@code Aggregator}.
   */
  public final T aggregateThenMaybeReset(
      long startEpochNanos, long epochNanos, Attributes attributes, boolean reset) {
    return doAggregateThenMaybeReset(
        startEpochNanos,
        epochNanos,
        attributes,
        exemplarReservoir.collectAndReset(attributes),
        reset);
  }

  /** Implementation of the {@link #aggregateThenMaybeReset(long, long, Attributes, boolean)} . */
  protected abstract T doAggregateThenMaybeReset(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<U> exemplars,
      boolean reset);

  public final void recordLong(long value, Attributes attributes, Context context) {
    exemplarReservoir.offerLongMeasurement(value, attributes, context);
    recordLong(value);
  }

  /**
   * Updates the current aggregator with a newly recorded {@code long} value.
   *
   * <p>Visible for Testing
   *
   * @param value the new {@code long} value to be added.
   */
  public final void recordLong(long value) {
    doRecordLong(value);
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of long
   * values.
   */
  protected void doRecordLong(long value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording long values.");
  }

  public final void recordDouble(double value, Attributes attributes, Context context) {
    exemplarReservoir.offerDoubleMeasurement(value, attributes, context);
    recordDouble(value);
  }

  /**
   * Updates the current aggregator with a newly recorded {@code double} value.
   *
   * <p>Visible for Testing
   *
   * @param value the new {@code double} value to be added.
   */
  public final void recordDouble(double value) {
    doRecordDouble(value);
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of
   * double values.
   */
  protected void doRecordDouble(double value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording double values.");
  }
}
