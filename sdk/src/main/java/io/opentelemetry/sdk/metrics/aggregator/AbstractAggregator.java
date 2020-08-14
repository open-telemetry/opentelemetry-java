/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.aggregator;

abstract class AbstractAggregator implements Aggregator {
  // Note: This is not 100% thread-safe. There is a race condition where recordings can
  // be made in the moment between the reset and the setting of this field's value. In those
  // cases, it is possible that a recording could be missed in a given recording interval, but
  // it should be picked up in the next, assuming that more recordings are being made.
  private volatile boolean hasRecordings = false;

  @Override
  public void mergeToAndReset(Aggregator other) {
    if (!this.getClass().isInstance(other)) {
      return;
    }
    doMergeAndReset(other);
    hasRecordings = false;
  }

  /**
   * Merges the current value into the given {@code aggregator} and resets the current value in this
   * {@code Aggregator}.
   *
   * <p>If this method is called, you can assume that the passed in aggregator can be cast to your
   * self-type.
   *
   * @param aggregator The aggregator to merge with.
   */
  abstract void doMergeAndReset(Aggregator aggregator);

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

  @Override
  public boolean hasRecordings() {
    return hasRecordings;
  }
}
