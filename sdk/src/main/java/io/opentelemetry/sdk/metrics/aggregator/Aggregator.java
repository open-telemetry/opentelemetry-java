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

import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Aggregator represents the interface for all the available aggregations. */
@ThreadSafe
public interface Aggregator {

  /**
   * Merges the current value into the given {@code aggregator} and resets the current value in this
   * {@code Aggregator}.
   *
   * @param aggregator value to merge into.
   */
  void mergeToAndReset(Aggregator aggregator);

  /**
   * Returns the {@code Point} with the given properties and the value from this Aggregation.
   *
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @param labels the labels for the {@code Point}.
   * @return the {@code Point} with the value from this Aggregation.
   */
  @Nullable
  Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels);

  /**
   * Updates the current aggregator with a newly recorded {@code long} value.
   *
   * @param value the new {@code long} value to be added.
   */
  void recordLong(long value);

  /**
   * Updates the current aggregator with a newly recorded {@code double} value.
   *
   * @param value the new {@code double} value to be added.
   */
  void recordDouble(double value);
}
