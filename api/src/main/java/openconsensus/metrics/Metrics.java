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

/**
 * Class for accessing the default {@link MetricRegistry}.
 *
 * @since 0.1.0
 */
public final class Metrics {
  private static final MetricRegistry METRIC_REGISTRY = NoopMetrics.newNoopMetricRegistry();

  /**
   * Returns the global {@link MetricRegistry} with the provided implementation.
   *
   * @return the global {@code MetricRegistry} with the provided implementation.
   * @since 0.1.0
   */
  public static MetricRegistry getMetricRegistry() {
    return METRIC_REGISTRY;
  }

  private Metrics() {}
}
