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
 * Base interface for all the Counter metrics.
 *
 * @param <H> the Handle.
 * @since 0.1.0
 */
public interface Counter<H> extends Metric<H> {

  /** Builder class for {@link Counter}. */
  interface Builder<B extends Counter.Builder<B, V>, V> extends Metric.Builder<B, V> {
    /**
     * Sets the monotonicity property for this {@code Metric}. If {@code true} only non-negative
     * values are expected.
     *
     * <p>Default value is {@code true}
     *
     * @param monotonic {@code true} only positive values are expected.
     * @return this.
     */
    B setMonotonic(boolean monotonic);
  }
}
