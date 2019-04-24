/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Metric {
  /**
   * Removes the {@code TimeSeries} from the metric, if it is present. i.e. references to previous
   * {@code TimeSeries} are invalid (not part of the metric).
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.1.0
   */
  void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all {@code TimeSeries} from the metric. i.e. references to all previous {@code
   * TimeSeries} are invalid (not part of the metric).
   *
   * @since 0.1.0
   */
  void clear();

  interface Builder<B extends Builder<B, V>, V> {
    /**
     * Sets the description of the Metric.
     *
     * <p>Default value is {@code ""}.
     *
     * @param description the description of the Metric.
     * @return this.
     */
    B setDescription(String description);

    /**
     * Sets the unit of the Metric.
     *
     * <p>Default value is {@code "1"}.
     *
     * @param unit the unit of the Metric.
     * @return this.
     */
    B setUnit(String unit);

    /**
     * Sets the list of label keys for the Metric.
     *
     * <p>Default value is {@link Collections#emptyList()}
     *
     * @param labelKeys the list of label keys for the Metric.
     * @return this.
     */
    B setLabelKeys(List<LabelKey> labelKeys);

    /**
     * Sets the map of constant labels (they will be added to all the TimeSeries) for the Metric.
     *
     * <p>Default value is {@link Collections#emptyMap()}.
     *
     * @param constantLabels the map of constant labels for the Metric.
     * @return this.
     */
    B setConstantLabels(Map<LabelKey, LabelValue> constantLabels);

    /**
     * Builds and returns a metric with the desired options.
     *
     * @return a metric with the desired options.
     */
    V build();
  }
}
