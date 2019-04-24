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

import openconsensus.resource.Resource;

/**
 * Creates and manages a set of metrics for a library/application.
 *
 * @since 0.1.0
 */
public interface MetricRegistry {

  /**
   * Returns a builder for a {@link LongGauge} to be added to the registry. This is more convenient
   * form when you want to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @return a {@code LongGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  LongGauge.Builder longGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DoubleGauge} to be added to the registry. This is more
   * convenient form when you want to manually increase and decrease values as per your service
   * requirements.
   *
   * @param name the name of the metric.
   * @return a {@code DoubleGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DoubleGauge.Builder doubleGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DerivedLongGauge} to be added to the registry. This is more
   * convenient form when you want to define a gauge by executing a {@link ToLongFunction} on an
   * object.
   *
   * @param name the name of the metric.
   * @return a {@code DerivedLongGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DerivedLongGauge.Builder derivedLongGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DerivedDoubleGauge} to be added to the registry. This is more
   * convenient form when you want to define a gauge by executing a {@link ToDoubleFunction} on an
   * object.
   *
   * @param name the name of the metric.
   * @return a {@code DerivedDoubleGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DerivedDoubleGauge.Builder derivedDoubleGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DoubleCumulative} to be added to the registry. This is a more
   * convenient form when you want to manually increase values as per your service requirements.
   *
   * @param name the name of the metric.
   * @return a {@code DoubleCumulative.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DoubleCumulative.Builder doubleCumulativeBuilder(String name);

  /**
   * Returns a builder for a {@link DerivedDoubleCumulative} to be added to the registry. This is a
   * more convenient form when you want to define a cumulative by executing a {@link
   * ToDoubleFunction} on an object.
   *
   * @param name the name of the metric.
   * @return a {@code DerivedDoubleCumulative.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DerivedDoubleCumulative.Builder derivedDoubleCumulativeBuilder(String name);

  /**
   * Returns a builder for a {@link LongCumulative} to be added to the registry. This is a more
   * convenient form when you want to manually increase values as per your service requirements.
   *
   * @param name the name of the metric.
   * @return a {@code LongCumulative.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  LongCumulative.Builder longCumulativeBuilder(String name);

  /**
   * Returns a builder for a {@link DerivedLongCumulative} to be added to the registry. This is a
   * more convenient form when you want to define a cumulative by executing a {@link ToLongFunction}
   * on an object.
   *
   * @param name the name of the metric.
   * @return a {@code DerivedLongCumulative.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  DerivedLongCumulative.Builder derivedLongCumulativeBuilder(String name);

  /** Builder class for the {@link MetricRegistry}. */
  interface Builder {

    /**
     * Sets the name of the component that reports these metrics.
     *
     * <p>The final name of the reported metric will be <code>component + "_" + name</code> if the
     * component is not empty.
     *
     * @param component the name of the component that reports these metrics.
     * @return this.
     */
    Builder setComponent(String component);

    /**
     * Sets the {@code Resource} associated with the new {@code MetricRegistry}.
     *
     * <p>This should be set only when reporting out-of-band metrics, otherwise the implementation
     * will set the {@code Resource} for in-process metrics.
     *
     * @param resource the {@code Resource} associated with the new {@code MetricRegistry}.
     * @return this.
     */
    Builder setResource(Resource resource);

    /**
     * Builds and returns a {@link MetricRegistry} with the desired options.
     *
     * @return a {@link MetricRegistry} with the desired options.
     */
    MetricRegistry build();
  }
}
