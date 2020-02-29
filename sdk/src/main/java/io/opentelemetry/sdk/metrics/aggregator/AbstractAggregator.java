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

  @Override
  public void mergeToAndReset(Aggregator other) {
    if (!this.getClass().isInstance(other)) {
      return;
    }
    doMergeAndReset(other);
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
  public void recordLong(long value) {
    throw new UnsupportedOperationException("This Aggregator does not support long values");
  }

  @Override
  public void recordDouble(double value) {
    throw new UnsupportedOperationException("This Aggregator does not support double values");
  }
}
