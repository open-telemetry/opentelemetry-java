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

/**
 * Base interface for all the Gauge metrics.
 *
 * @param <H> the Handle.
 * @since 0.1.0
 */
public interface Gauge<H> extends Metric<H> {

  /** Builder class for {@link Gauge}. */
  interface Builder<B extends Gauge.Builder<B, V>, V> extends Metric.Builder<B, V> {
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
