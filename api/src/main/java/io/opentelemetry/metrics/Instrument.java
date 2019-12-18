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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Base interface for all metrics defined in this package.
 *
 * @param <B> the Bound type.
 * @since 0.1.0
 */
@ThreadSafe
public interface Instrument<B> {
  /**
   * Returns a {@code Bound} with associated with specified {@code labelSet}. Multiples requests
   * with the same {@code labelSet} may return the same {@code Bound}.
   *
   * <p>It is recommended to keep a reference to the Bound instead of always calling this method for
   * every operations.
   *
   * @param labelSet the set of labels.
   * @return a {@code Bound} value of the metric.
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.1.0
   */
  B bind(LabelSet labelSet);

  /**
   * Removes the {@code Bound} from the metric. i.e. references to previous {@code Bound} are
   * invalid (not part of the metric).
   *
   * @param bound the {@code Bound} to be removed.
   * @since 0.1.0
   */
  void unbind(B bound);

  /**
   * The {@code Builder} class for the {@code Instrument}.
   *
   * @param <B> the specific builder object.
   * @param <V> the return value for {@code build()}.
   */
  interface Builder<B extends Builder<B, V>, V> {
    /**
     * Sets the description of the {@code Instrument}.
     *
     * <p>Default value is {@code ""}.
     *
     * @param description the description of the Instrument.
     * @return this.
     */
    B setDescription(String description);

    /**
     * Sets the unit of the {@code Instrument}.
     *
     * <p>Default value is {@code "1"}.
     *
     * @param unit the unit of the Instrument.
     * @return this.
     */
    B setUnit(String unit);

    /**
     * Sets the list of label keys for the Instrument.
     *
     * <p>Default value is {@link Collections#emptyList()}
     *
     * @param labelKeys the list of label keys for the Instrument.
     * @return this.
     */
    B setLabelKeys(List<String> labelKeys);

    /**
     * Sets the map of constant labels (they will be added to all the Bound) for the Instrument.
     *
     * <p>Default value is {@link Collections#emptyMap()}.
     *
     * @param constantLabels the map of constant labels for the Instrument.
     * @return this.
     */
    B setConstantLabels(Map<String, String> constantLabels);

    /**
     * Builds and returns a {@code Instrument} with the desired options.
     *
     * @return a {@code Instrument} with the desired options.
     */
    V build();
  }
}
