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
public abstract class MetricRegistry {

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
  public abstract LongGauge addLongGauge(String name, MetricOptions options);

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
  public abstract DoubleGauge addDoubleGauge(String name, MetricOptions options);

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
  public abstract DerivedLongGauge addDerivedLongGauge(String name, MetricOptions options);

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
  public abstract DerivedDoubleGauge addDerivedDoubleGauge(String name, MetricOptions options);

  /**
   * Builds a new double cumulative to be added to the registry. This is a more convenient form when
   * you want to manually increase values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DoubleCumulative}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  public abstract DoubleCumulative addDoubleCumulative(String name, MetricOptions options);

  /**
   * Builds a new derived double cumulative to be added to the registry. This is a more convenient
   * form when you want to define a cumulative by executing a {@link ToDoubleFunction} on an object.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DerivedDoubleCumulative}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  public abstract DerivedDoubleCumulative addDerivedDoubleCumulative(
      String name, MetricOptions options);

  /**
   * Builds a new long cumulative to be added to the registry. This is a more convenient form when
   * you want to manually increase values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code LongCumulative}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  public abstract LongCumulative addLongCumulative(String name, MetricOptions options);

  /**
   * Builds a new derived long cumulative to be added to the registry. This is a more convenient
   * form when you want to define a cumulative by executing a {@link ToLongFunction} on an object.
   *
   * @param name the name of the metric.
   * @param options the options for the metric.
   * @return a {@code DerivedLongCumulative}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  public abstract DerivedLongCumulative addDerivedLongCumulative(
      String name, MetricOptions options);

  /** Builder class for the {@link MetricRegistry}. */
  public abstract static class Builder {

    /**
     * Sets the name of the component that reports these metrics.
     *
     * <p>The final name of the reported metric will be <code>component + "_" + name</code> if the
     * component is not empty.
     *
     * @param component the name of the component that reports these metrics.
     * @return this.
     */
    public abstract Builder setComponent(String component);

    /**
     * Sets the {@code Resource} associated with the new {@code MetricRegistry}.
     *
     * <p>This should be set only when reporting out-of-band metrics, otherwise the implementation
     * will set the {@code Resource} for in-process metrics.
     *
     * @param resource the {@code Resource} associated with the new {@code MetricRegistry}.
     * @return this.
     */
    public abstract Builder setResource(Resource resource);

    /**
     * Builds and returns a {@link MetricRegistry} with the desired options.
     *
     * @return a {@link MetricRegistry} with the desired options.
     */
    public abstract MetricRegistry build();
  }
}
