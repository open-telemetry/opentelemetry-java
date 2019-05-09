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

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.opencensusshim.common.ExperimentalApi;
import io.opentelemetry.opencensusshim.common.ToDoubleFunction;
import io.opentelemetry.opencensusshim.common.ToLongFunction;
import io.opentelemetry.opencensusshim.internal.Utils;
import io.opentelemetry.opencensusshim.metrics.export.MetricProducer;
import io.opentelemetry.opencensusshim.metrics.export.MetricProducerManager;
import java.util.List;

/**
 * Creates and manages your application's set of metrics. The default implementation of this creates
 * a {@link MetricProducer} and registers it to the global {@link MetricProducerManager}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class MetricRegistry {
  /**
   * This will be removed in 0.22.
   *
   * @deprecated since 0.20, use {@link #addLongGauge(String, MetricOptions)}.
   * @since 0.1.0
   */
  @Deprecated
  public LongGauge addLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return addLongGauge(
        name,
        MetricOptions.builder()
            .setDescription(description)
            .setUnit(unit)
            .setLabelKeys(labelKeys)
            .build());
  }

  /**
   * Builds a new long gauge to be added to the registry. This is more convenient form when you want
   * to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code LongGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract LongGauge addLongGauge(String name, MetricOptions options);

  /**
   * This will be removed in 0.22.
   *
   * @deprecated since 0.20, use {@link #addDoubleGauge(String, MetricOptions)}.
   * @since 0.1.0
   */
  @Deprecated
  public DoubleGauge addDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return addDoubleGauge(
        name,
        MetricOptions.builder()
            .setDescription(description)
            .setUnit(unit)
            .setLabelKeys(labelKeys)
            .build());
  }

  /**
   * Builds a new double gauge to be added to the registry. This is more convenient form when you
   * want to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DoubleGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DoubleGauge addDoubleGauge(String name, MetricOptions options);

  /**
   * This will be removed in 0.22.
   *
   * @deprecated since 0.20, use {@link #addDerivedLongGauge(String, MetricOptions)}.
   * @since 0.1.0
   */
  @Deprecated
  public DerivedLongGauge addDerivedLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return addDerivedLongGauge(
        name,
        MetricOptions.builder()
            .setDescription(description)
            .setUnit(unit)
            .setLabelKeys(labelKeys)
            .build());
  }

  /**
   * Builds a new derived long gauge to be added to the registry. This is more convenient form when
   * you want to define a gauge by executing a {@link ToLongFunction} on an object.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DerivedLongGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DerivedLongGauge addDerivedLongGauge(String name, MetricOptions options);

  /**
   * This will be removed in 0.22.
   *
   * @deprecated since 0.20, use {@link #addDerivedDoubleGauge(String, MetricOptions)}.
   * @since 0.1.0
   */
  @Deprecated
  public DerivedDoubleGauge addDerivedDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return addDerivedDoubleGauge(
        name,
        MetricOptions.builder()
            .setDescription(description)
            .setUnit(unit)
            .setLabelKeys(labelKeys)
            .build());
  }

  /**
   * Builds a new derived double gauge to be added to the registry. This is more convenient form
   * when you want to define a gauge by executing a {@link ToDoubleFunction} on an object.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DerivedDoubleGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DerivedDoubleGauge addDerivedDoubleGauge(String name, MetricOptions options);

  static MetricRegistry newNoopMetricRegistry() {
    return new NoopMetricRegistry();
  }

  private static final class NoopMetricRegistry extends MetricRegistry {

    @Override
    public LongGauge addLongGauge(String name, MetricOptions options) {
      return LongGauge.newNoopLongGauge(
          Utils.checkNotNull(name, "name"),
          options.getDescription(),
          options.getUnit(),
          options.getLabelKeys());
    }

    @Override
    public DoubleGauge addDoubleGauge(String name, MetricOptions options) {
      return DoubleGauge.newNoopDoubleGauge(
          Utils.checkNotNull(name, "name"),
          options.getDescription(),
          options.getUnit(),
          options.getLabelKeys());
    }

    @Override
    public DerivedLongGauge addDerivedLongGauge(String name, MetricOptions options) {
      return DerivedLongGauge.newNoopDerivedLongGauge(
          Utils.checkNotNull(name, "name"),
          options.getDescription(),
          options.getUnit(),
          options.getLabelKeys());
    }

    @Override
    public DerivedDoubleGauge addDerivedDoubleGauge(String name, MetricOptions options) {
      return DerivedDoubleGauge.newNoopDerivedDoubleGauge(
          Utils.checkNotNull(name, "name"),
          options.getDescription(),
          options.getUnit(),
          options.getLabelKeys());
    }
  }
}
