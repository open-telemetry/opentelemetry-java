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
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.metrics.internal.exemplar.LongExemplarReservoir;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
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
public abstract class AggregatorHandle<T extends PointData> implements RecordOp {

  private static final String UNSUPPORTED_LONG_MESSAGE =
      "This aggregator does not support long values.";
  private static final String UNSUPPORTED_DOUBLE_MESSAGE =
      "This aggregator does not support double values.";

  private final long creationEpochNanos;
  // A reservoir of sampled exemplars for this time period.
  @Nullable private final DoubleExemplarReservoir doubleReservoirFactory;
  @Nullable private final LongExemplarReservoir longReservoirFactory;
  private final boolean isDoubleType;
  private volatile boolean valuesRecorded = false;
  // The processed attributes associated with this handle's series. Set by synchronous metric
  // storage when the handle is first created or (for pooled delta handles) when reused for a new
  // series. Volatile so that setAttributes() is visible across threads without requiring
  // external synchronization.
  @Nullable private volatile Attributes attributes;

  protected AggregatorHandle(
      long creationEpochNanos, ExemplarReservoirFactory reservoirFactory, boolean isDoubleType) {
    this.creationEpochNanos = creationEpochNanos;
    this.isDoubleType = isDoubleType;
    if (isDoubleType) {
      this.doubleReservoirFactory = reservoirFactory.createDoubleExemplarReservoir();
      this.longReservoirFactory = null;
    } else {
      this.doubleReservoirFactory = null;
      this.longReservoirFactory = reservoirFactory.createLongExemplarReservoir();
    }
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
          throwUnsupportedIfNull(this.doubleReservoirFactory, UNSUPPORTED_DOUBLE_MESSAGE)
              .collectAndResetDoubles(attributes),
          reset);
    }
    return doAggregateThenMaybeResetLongs(
        startEpochNanos,
        epochNanos,
        attributes,
        throwUnsupportedIfNull(this.longReservoirFactory, UNSUPPORTED_LONG_MESSAGE)
            .collectAndResetLongs(attributes),
        reset);
  }

  /** Implementation of the {@link #aggregateThenMaybeReset(long, long, Attributes, boolean)} . */
  protected T doAggregateThenMaybeResetDoubles(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars,
      boolean reset) {
    throw new UnsupportedOperationException(UNSUPPORTED_DOUBLE_MESSAGE);
  }

  /** Implementation of the {@link #aggregateThenMaybeReset(long, long, Attributes, boolean)} . */
  protected T doAggregateThenMaybeResetLongs(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<LongExemplarData> exemplars,
      boolean reset) {
    throw new UnsupportedOperationException(UNSUPPORTED_LONG_MESSAGE);
  }

  /** {@inheritDoc} Delegates to {@link #recordLong(long, Attributes, Context)}. */
  @Override
  public void recordLong(long value) {
    recordLong(
        value,
        Objects.requireNonNull(attributes, "setAttributes must be called before recordLong"),
        Context.current());
  }

  public void recordLong(long value, Attributes attributes, Context context) {
    throwUnsupportedIfNull(this.longReservoirFactory, UNSUPPORTED_LONG_MESSAGE)
        .offerLongMeasurement(value, attributes, context);
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

  /** {@inheritDoc} Delegates to {@link #recordDouble(double, Attributes, Context)}. */
  @Override
  public void recordDouble(double value) {
    recordDouble(
        value,
        Objects.requireNonNull(attributes, "setAttributes must be called before recordDouble"),
        Context.current());
  }

  public final void recordDouble(double value, Attributes attributes, Context context) {
    throwUnsupportedIfNull(this.doubleReservoirFactory, UNSUPPORTED_DOUBLE_MESSAGE)
        .offerDoubleMeasurement(value, attributes, context);
    doRecordDouble(value);
    valuesRecorded = true;
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of
   * double values.
   */
  protected void doRecordDouble(double value) {
    throw new UnsupportedOperationException(UNSUPPORTED_DOUBLE_MESSAGE);
  }

  /**
   * Checks whether this handle has values recorded.
   *
   * @return True if values has been recorded to it
   */
  public boolean hasRecordedValues() {
    return valuesRecorded;
  }

  private static <S> S throwUnsupportedIfNull(@Nullable S value, String message) {
    if (value == null) {
      throw new UnsupportedOperationException(message);
    }
    return value;
  }

  /**
   * Returns the epoch timestamp (nanos) at which this handle was created.
   *
   * <p>For cumulative synchronous instruments, this is the time of the first measurement for the
   * series and is used as {@link PointData#getStartEpochNanos()}.
   *
   * <p>For cumulative asynchronous instruments, this is either the instrument creation time (if the
   * series first appeared during the first collection cycle) or the preceding collection interval's
   * timestamp (if the series appeared in a later cycle), and is used as {@link
   * PointData#getStartEpochNanos()}.
   *
   * <p>Not used for delta instruments; their start epoch is computed directly from the reader's
   * last collection time or instrument creation time.
   */
  public long getCreationEpochNanos() {
    return creationEpochNanos;
  }

  /**
   * Sets the attributes for this handle's series. Called by synchronous metric storage when the
   * handle is created (cumulative) or when a pooled handle is reused for a new series (delta).
   */
  public void setAttributes(Attributes attributes) {
    this.attributes = attributes;
  }

  /** Returns the attributes associated with this handle's series. */
  public Attributes getAttributes() {
    return Objects.requireNonNull(attributes, "setAttributes must be called before getAttributes");
  }
}
