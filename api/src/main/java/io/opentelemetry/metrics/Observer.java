/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Base interface for all the Observer metrics.
 *
 * @param <R> the Handle.
 * @since 0.1.0
 */
public interface Observer<R> extends Metric<Observer.Handle> {
  /**
   * A {@code Handle} for a {@code Observer}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface Handle {}

  /**
   * A {@code Callback} for a {@code Observer}.
   *
   * @since 0.1.0
   */
  interface Callback<R> {
    void update(R result);
  }

  /**
   * Sets a callback that gets executed every time before exporting this metric.
   *
   * <p>Evaluation is deferred until needed, if this {@code Observer} metric is not exported then it
   * will never be called.
   *
   * @param metricUpdater the callback to be executed before export.
   * @since 0.1.0
   */
  void setCallback(Callback<R> metricUpdater);

  /** Builder class for {@link Observer}. */
  interface Builder<B extends Metric.Builder<B, V>, V> extends Metric.Builder<B, V> {
    /**
     * Sets the monotonicity property for this {@code Metric}. If {@code true} successive values are
     * expected to rise monotonically.
     *
     * <p>Default value is {@code false}
     *
     * @param monotonic {@code true} successive values are expected to rise monotonically.
     * @return this.
     */
    B setMonotonic(boolean monotonic);
  }
}
