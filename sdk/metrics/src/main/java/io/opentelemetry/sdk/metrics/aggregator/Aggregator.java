/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/**
 * Aggregator represents the abstract class for all the available aggregations that can be computed
 * during the accumulation phase for a synchronous instrument.
 *
 * <p>It atomically counts the number of references (usages) while also keeping a state of
 * mapped/unmapped into an external map. It uses an atomic value where the least significant bit is
 * used to keep the state of mapping ('1' is used for unmapped and '0' is for mapped) and the rest
 * of the bits are used for reference (usage) counting.
 */
public abstract class Aggregator<T extends Accumulation> {
  // Atomically counts the number of references (usages) while also keeping a state of
  // mapped/unmapped into a registry map.
  private final AtomicLong refCountMapped;
  // Note: This is not 100% thread-safe. There is a race condition where recordings can
  // be made in the moment between the reset and the setting of this field's value. In those
  // cases, it is possible that a recording could be missed in a given recording interval, but
  // it should be picked up in the next, assuming that more recordings are being made.
  private volatile boolean hasRecordings = false;

  protected Aggregator() {
    // Start with this binding already bound.
    this.refCountMapped = new AtomicLong(2);
  }

  /**
   * Acquires this {@code Aggregator} for use. Returns {@code true} if the entry is still mapped and
   * increases the reference usages, if unmapped returns {@code false}.
   *
   * @return {@code true} if successful.
   */
  public final boolean acquire() {
    // Every reference adds/removes 2 instead of 1 to avoid changing the mapping bit.
    return (refCountMapped.addAndGet(2L) & 1L) == 0;
  }

  /** Release this {@code Aggregator}. It decreases the reference usage. */
  public final void release() {
    // Every reference adds/removes 2 instead of 1 to avoid changing the mapping bit.
    refCountMapped.getAndAdd(-2L);
  }

  /**
   * Flips the mapped bit to "unmapped" state and returns true if both of the following conditions
   * are true upon entry to this function: 1) There are no active references; 2) The mapped bit is
   * in "mapped" state; otherwise no changes are done to mapped bit and false is returned.
   *
   * @return {@code true} if successful.
   */
  public final boolean tryUnmap() {
    if (refCountMapped.get() != 0) {
      // Still references (usages) to this bound or already unmapped.
      return false;
    }
    return refCountMapped.compareAndSet(0L, 1L);
  }

  /**
   * Returns the current value into as {@link Accumulation} and resets the current value in this
   * {@code Aggregator}.
   */
  @Nullable
  public final T accumulateThenReset() {
    if (!hasRecordings) {
      return null;
    }
    hasRecordings = false;
    return doAccumulateThenReset();
  }

  /** Implementation of the {@code accumulateThenReset}. */
  protected abstract T doAccumulateThenReset();

  /**
   * Updates the current aggregator with a newly recorded {@code long} value.
   *
   * @param value the new {@code long} value to be added.
   */
  public final void recordLong(long value) {
    doRecordLong(value);
    hasRecordings = true;
  }

  /**
   * Concrete Aggregator instances should implement this method in order support recordings of long
   * values.
   */
  protected void doRecordLong(long value) {
    throw new UnsupportedOperationException(
        "This aggregator does not support recording long values.");
  }

  /**
   * Updates the current aggregator with a newly recorded {@code double} value.
   *
   * @param value the new {@code double} value to be added.
   */
  public final void recordDouble(double value) {
    doRecordDouble(value);
    hasRecordings = true;
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
