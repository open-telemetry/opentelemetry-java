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

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.Utils;
import openconsensus.metrics.data.LabelKey;
import openconsensus.metrics.data.LabelValue;

/**
 * Options for every metric added to the {@link MetricRegistry}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class MetricOptions {

  /**
   * Returns the description of the Metric.
   *
   * <p>Default value is {@code ""}.
   *
   * @return the description of the Metric.
   */
  public abstract String getDescription();

  /**
   * Returns the unit of the Metric.
   *
   * <p>Default value is {@code "1"}.
   *
   * @return the unit of the Metric.
   */
  public abstract String getUnit();

  /**
   * Returns the list of label keys for the Metric.
   *
   * <p>Default value is {@link Collections#emptyList()}.
   *
   * @return the list of label keys for the Metric.
   */
  public abstract List<LabelKey> getLabelKeys();

  /**
   * Returns the map of constant labels (they will be added to all the TimeSeries) for the Metric.
   *
   * <p>Default value is {@link Collections#emptyMap()}.
   *
   * @return the map of constant labels for the Metric.
   */
  // TODO: add support for this and make it public.
  abstract Map<LabelKey, LabelValue> getConstantLabels();

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   * @since 0.1.0
   */
  public static Builder builder() {
    return new AutoValue_MetricOptions.Builder()
        .setDescription("")
        .setUnit("1")
        .setLabelKeys(Collections.<LabelKey>emptyList())
        .setConstantLabels(Collections.<LabelKey, LabelValue>emptyMap());
  }

  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Sets the description of the Metric.
     *
     * @param description the description of the Metric.
     * @return this.
     */
    public abstract Builder setDescription(String description);

    /**
     * Sets the unit of the Metric.
     *
     * @param unit the unit of the Metric.
     * @return this.
     */
    public abstract Builder setUnit(String unit);

    /**
     * Sets the list of label keys for the Metric.
     *
     * @param labelKeys the list of label keys for the Metric.
     * @return this.
     */
    public abstract Builder setLabelKeys(List<LabelKey> labelKeys);

    /**
     * Sets the map of constant labels (they will be added to all the TimeSeries) for the Metric.
     *
     * @param constantLabels the map of constant labels for the Metric.
     * @return this.
     */
    // TODO: add support for this and make it public.
    abstract Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels);

    abstract Map<LabelKey, LabelValue> getConstantLabels();

    abstract List<LabelKey> getLabelKeys();

    abstract MetricOptions autoBuild();

    /**
     * Builds and returns a {@code MetricOptions} with the desired options.
     *
     * @return a {@code MetricOptions} with the desired options.
     * @since 0.1.0
     * @throws NullPointerException if {@code description}, OR {@code unit} is null, OR {@code
     *     labelKeys} is null OR any element of {@code labelKeys} is null, OR OR {@code
     *     constantLabels} is null OR any element of {@code constantLabels} is null.
     * @throws IllegalArgumentException if any {@code LabelKey} from the {@code labelKeys} is in the
     *     {@code constantLabels}.
     */
    public MetricOptions build() {
      setLabelKeys(Collections.unmodifiableList(new ArrayList<LabelKey>(getLabelKeys())));
      setConstantLabels(
          Collections.unmodifiableMap(
              new LinkedHashMap<LabelKey, LabelValue>(getConstantLabels())));
      MetricOptions options = autoBuild();
      Utils.checkListElementNotNull(options.getLabelKeys(), "labelKeys elements");
      Utils.checkMapElementNotNull(options.getConstantLabels(), "constantLabels elements");

      HashSet<String> labelKeyNamesMap = new HashSet<String>();
      for (LabelKey labelKey : options.getLabelKeys()) {
        if (labelKeyNamesMap.contains(labelKey.getKey())) {
          throw new IllegalArgumentException("Invalid LabelKey in labelKeys");
        }
        labelKeyNamesMap.add(labelKey.getKey());
      }
      for (Map.Entry<LabelKey, LabelValue> constantLabel : options.getConstantLabels().entrySet()) {
        if (labelKeyNamesMap.contains(constantLabel.getKey().getKey())) {
          throw new IllegalArgumentException("Invalid LabelKey in constantLabels");
        }
        labelKeyNamesMap.add(constantLabel.getKey().getKey());
      }
      return options;
    }

    Builder() {}
  }

  MetricOptions() {}
}
