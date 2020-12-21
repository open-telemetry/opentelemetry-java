/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import javax.annotation.Nullable;

abstract class AbstractAggregator implements Aggregator {
  // Note: This is not 100% thread-safe. There is a race condition where recordings can
  // be made in the moment between the reset and the setting of this field's value. In those
  // cases, it is possible that a recording could be missed in a given recording interval, but
  // it should be picked up in the next, assuming that more recordings are being made.
  private volatile boolean hasRecordings = false;

  @Override
  @Nullable
  public Accumulation accumulateThenReset() {
    if (!hasRecordings) {
      return null;
    }
    hasRecordings = false;
    return doAccumulateThenReset();
  }

  /** Implementation of the {@code accumulateThenReset}. */
  abstract Accumulation doAccumulateThenReset();

  @Override
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

  @Override
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
