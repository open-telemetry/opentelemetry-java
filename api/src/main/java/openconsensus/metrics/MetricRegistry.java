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

import openconsensus.common.ExperimentalApi;
import openconsensus.common.ToDoubleFunction;
import openconsensus.metrics.producer.MetricProducer;

/**
 * Creates and manages your application's set of metrics.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class MetricRegistry extends MetricProducer {

  /**
   * Builds a new gauge to be added to the registry. This is more convenient form when you want to
   * manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DoubleGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract Gauge addGauge(String name, MetricOptions options);

  /**
   * Builds a new derived gauge to be added to the registry. This is more convenient form when you
   * want to define a gauge by executing a {@link ToDoubleFunction} on an object.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DerivedDoubleGauge}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  @ExperimentalApi
  public abstract DerivedGauge addDerivedGauge(String name, MetricOptions options);
}
