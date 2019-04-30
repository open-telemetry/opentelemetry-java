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

/** Entry point fot metrics API, this object allows to record measurements and {@link Metric}s. */
public interface Meter {

  /**
   * Returns a builder for a {@link GaugeLong} to be added to the registry.
   *
   * @param name the name of the metric.
   * @return a {@code GaugeLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  GaugeLong.Builder gaugeLongBuilder(String name);

  /**
   * Returns a builder for a {@link GaugeDouble} to be added to the registry.
   *
   * @param name the name of the metric.
   * @return a {@code GaugeDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  GaugeDouble.Builder gaugeDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterDouble} to be added to the registry.
   *
   * @param name the name of the metric.
   * @return a {@code CounterDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  CounterDouble.Builder counterDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterLong} to be added to the registry.
   *
   * @param name the name of the metric.
   * @return a {@code CounterLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  CounterLong.Builder counterLongBuilder(String name);
}
