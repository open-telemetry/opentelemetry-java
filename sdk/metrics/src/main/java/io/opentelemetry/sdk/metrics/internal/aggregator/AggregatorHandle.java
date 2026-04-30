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
import java.util.concurrent.atomic.AtomicInteger;
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
public abstract class AggregatorHandle<T extends PointData> {

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

  // Delta coordination: null for cumulative handles (never initDelta()'d).
  // Guards per-handle recording using an even/odd protocol:
  //   - Recording threads increment by 2 before recording, decrement by 2 when done.
  //   - The collect thread increments by 1 (making the count odd) as a signal that this
  //     handle is being collected; recorders that observe an odd count release and retry.
  //   - Once all in-flight recordings finish the count returns to 1, and the collect
  //     thread decrements by 1 to restore it to even for the next cycle.
  //
  // TODO: consider passing temporality (delta vs cumulative) as a constructor parameter so
  //   this field can be final (always non-null) and the @Nullable volatile overhead goes away.
  //   That would require Aggregator.createHandle() to accept a boolean/enum, touching all
  //   aggregator implementations, but would yield a cleaner memory model for all handles.
  @Nullable private volatile AtomicInteger state;

  // Whether this handle was obtained via bind(). Bound handles survive holder swaps in delta
  // IMMUTABLE_DATA mode rather than being abandoned at the end of each collection interval.
  public volatile boolean bound = false;

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
   * Initialises this handle for use in delta metric storage. Must be called once after {@link
   * Aggregator#createHandle} before the handle is inserted into a holder map.
   */
  public void initDelta() {
    this.state = new AtomicInteger(0);
  }

  // ---------------------------------------------------------------------------
  // Delta spin-lock protocol (no-ops / always-true for cumulative handles)
  // ---------------------------------------------------------------------------

  /**
   * Tries to acquire a recording slot. Returns false if the collector has locked this handle (odd
   * state); the caller should retry with the new holder.
   */
  public boolean tryAcquireForRecord() {
    AtomicInteger s = state;
    if (s == null) {
      return true; // cumulative: no coordination needed
    }
    int v = s.addAndGet(2);
    if ((v & 1) != 0) {
      s.addAndGet(-2);
      return false;
    }
    return true;
  }

  /**
   * Acquires a recording slot unconditionally. Only safe to call while the holder gate is held,
   * which prevents the collector from starting its lock pass.
   */
  public void acquireForRecord() {
    Objects.requireNonNull(state).addAndGet(2);
  }

  /**
   * Releases a recording slot acquired via {@link #tryAcquireForRecord()} or {@link
   * #acquireForRecord()}.
   */
  public void releaseRecord() {
    Objects.requireNonNull(state).addAndGet(-2);
  }

  /** Signals that collection is starting. Recorders that observe this will abort and retry. */
  public void lockForCollect() {
    Objects.requireNonNull(state).addAndGet(1);
  }

  /** Waits for all in-flight recorders to finish, then clears the collection lock. */
  public void awaitRecordersAndUnlock() {
    AtomicInteger s = Objects.requireNonNull(state);
    while (s.get() > 1) {}
    s.addAndGet(-1);
  }

  /**
   * Waits for all in-flight recorders to finish WITHOUT clearing the collection lock. Used by the
   * collect thread for bound handles so that the accumulator can be aggregated and reset while no
   * recordings are in-flight, before recordings resume against the freshly-reset accumulator.
   */
  public void awaitRecorders() {
    AtomicInteger s = Objects.requireNonNull(state);
    while (s.get() > 1) {}
  }

  /**
   * Clears the collection lock after aggregation is complete. Must be called after {@link
   * #awaitRecorders()}. The happens-before edge from this write to the next {@link
   * #tryAcquireForRecord()} ensures recording threads see any state changes made during the locked
   * window.
   */
  public void unlockAfterCollect() {
    Objects.requireNonNull(state).addAndGet(-1);
  }

  // ---------------------------------------------------------------------------
  // Recording
  // ---------------------------------------------------------------------------

  /**
   * Records a long value using the handle's bound attributes. For delta handles, uses the spin-lock
   * protocol to coordinate with the collect thread.
   */
  public void recordLong(long value) {
    AtomicInteger s = state;
    if (s == null) {
      // Cumulative: record directly, no coordination needed.
      recordLong(
          value,
          Objects.requireNonNull(attributes, "setAttributes must be called before recordLong"),
          Context.current());
      return;
    }
    // Delta: spin until we can acquire a recording slot.
    while (true) {
      int v = s.addAndGet(2);
      if ((v & 1) == 0) {
        try {
          recordLong(
              value,
              Objects.requireNonNull(attributes, "setAttributes must be called before recordLong"),
              Context.current());
        } finally {
          s.addAndGet(-2);
        }
        return;
      }
      s.addAndGet(-2);
    }
  }

  /**
   * Records a double value using the handle's bound attributes. For delta handles, uses the
   * spin-lock protocol to coordinate with the collect thread.
   */
  public void recordDouble(double value) {
    AtomicInteger s = state;
    if (s == null) {
      // Cumulative: record directly, no coordination needed.
      recordDouble(
          value,
          Objects.requireNonNull(attributes, "setAttributes must be called before recordDouble"),
          Context.current());
      return;
    }
    // Delta: spin until we can acquire a recording slot.
    while (true) {
      int v = s.addAndGet(2);
      if ((v & 1) == 0) {
        try {
          recordDouble(
              value,
              Objects.requireNonNull(
                  attributes, "setAttributes must be called before recordDouble"),
              Context.current());
        } finally {
          s.addAndGet(-2);
        }
        return;
      }
      s.addAndGet(-2);
    }
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

  // ---------------------------------------------------------------------------
  // Aggregation
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // Metadata
  // ---------------------------------------------------------------------------

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
