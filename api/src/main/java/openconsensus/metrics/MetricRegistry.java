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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import openconsensus.common.ExperimentalApi;
import openconsensus.common.ToDoubleFunction;
import openconsensus.common.ToLongFunction;
import openconsensus.internal.Utils;
import openconsensus.metrics.data.LabelKey;
import openconsensus.metrics.data.Metric;
import openconsensus.metrics.producer.MetricProducer;

/**
 * Creates and manages your application's set of metrics.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class MetricRegistry extends MetricProducer {
  /**
   * Builds a new long gauge to be added to the registry. This is more convenient form when you want
   * to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @return a {@code LongGauge}.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract LongGauge addLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  /**
   * Builds a new double gauge to be added to the registry. This is more convenient form when you
   * want to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @return a {@code DoubleGauge}.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DoubleGauge addDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  /**
   * Builds a new derived long gauge to be added to the registry. This is more convenient form when
   * you want to define a gauge by executing a {@link ToLongFunction} on an object.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @return a {@code DerivedLongGauge}.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DerivedLongGauge addDerivedLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  /**
   * Builds a new derived double gauge to be added to the registry. This is more convenient form
   * when you want to define a gauge by executing a {@link ToDoubleFunction} on an object.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @return a {@code DerivedDoubleGauge}.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DerivedDoubleGauge addDerivedDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  static MetricRegistry newNoopMetricRegistry() {
    return new NoopMetricRegistry();
  }

  private static final class NoopMetricRegistry extends MetricRegistry {

    @Override
    public LongGauge addLongGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      return LongGauge.newNoopLongGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public DoubleGauge addDoubleGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      return DoubleGauge.newNoopDoubleGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public DerivedLongGauge addDerivedLongGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      return DerivedLongGauge.newNoopDerivedLongGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public DerivedDoubleGauge addDerivedDoubleGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      return DerivedDoubleGauge.newNoopDerivedDoubleGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public Collection<Metric> getMetrics() {
      return Collections.emptyList();
    }
  }
}
