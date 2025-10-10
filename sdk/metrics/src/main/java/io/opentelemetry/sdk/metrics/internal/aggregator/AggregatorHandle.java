/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
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
public abstract class AggregatorHandle<T extends PointData> {

  // A reservoir of sampled exemplars for this time period.
  private final ExemplarReservoir exemplarReservoir;
  private volatile boolean valuesRecorded = false;
  private final boolean isDoubleType;

  protected AggregatorHandle(ExemplarReservoir exemplarReservoir) {
    this.exemplarReservoir = exemplarReservoir;
    this.isDoubleType = isDoubleType();
  }

  /**
   * Returns the current value into as {@link T}. If {@code reset} is {@code true}, resets the
   * current value in this {@code Aggregator}.
   */
  public final T aggregateThenMaybeReset(
      long startEpochNanos, long epochNanos, Attributes attributes, boolean reset) {
    if (reset) {
      valuesRecorded = false;
    }

    if (isDoubleType) {
      return doAggregateThenMaybeResetDoubles(
          startEpochNanos,
          epochNanos,
          attributes,
          exemplarReservoir.collectAndResetDoubles(attributes),
          reset);
    }
    return doAggregateThenMaybeResetLongs(
        startEpochNanos,
        epochNanos,
        attributes,
        exemplarReservoir.collectAndResetLongs(attributes),
        reset);
  }

  /**
   * Indicates whether this {@link AggregatorHandle} supports double or long values.
   *
   * <p>If it supports doubles, it MUST implement {@link #doAggregateThenMaybeResetDoubles(long,
   * long, Attributes, List, boolean)} and {@link #doRecordDouble(double)}.
   *
   * <p>If it supports long, it MUST implement {@link #doAggregateThenMaybeResetLongs(long, long,
   * Attributes, List, boolean)} and {@link #doRecordLong(long)}.
   *
   * @return true if it supports doubles, false if it supports longs.
   */
  protected abstract boolean isDoubleType();

  /** Implementation of the {@link #aggregateThenMaybeReset(long, long, Attributes, boolean)} . */
  protected T doAggregateThenMaybeResetDoubles(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars,
      boolean reset) {
    throw new UnsupportedOperationException("This aggregator does not support double values.");
  }

  /** Implementation of the {@link #aggregateThenMaybeReset(long, long, Attributes, boolean)} . */
  protected T doAggregateThenMaybeResetLongs(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<LongExemplarData> exemplars,
      boolean reset) {
    throw new UnsupportedOperationException("This aggregator does not support long values.");
  }

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
    valuesRecorded = true;
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of long
   * values.
   */
  protected void doRecordLong(long value) {
    throw new UnsupportedOperationException("This aggregator does not support long values.");
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
    valuesRecorded = true;
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of
   * double values.
   */
  protected void doRecordDouble(double value) {
    throw new UnsupportedOperationException("This aggregator does not support double values.");
  }

  /**
   * Checks whether this handle has values recorded.
   *
   * @return True if values has been recorded to it
   */
  public boolean hasRecordedValues() {
    return valuesRecorded;
  }
}
