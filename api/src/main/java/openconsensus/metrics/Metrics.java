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
import openconsensus.metrics.export.ExportComponent;
import openconsensus.metrics.export.MetricProducerManager;

/**
 * Class for accessing the default {@link MetricsComponent}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public final class Metrics {
  private static final MetricsComponent metricsComponent =MetricsComponent.newNoopMetricsComponent();

  /**
   * Returns the global {@link ExportComponent}.
   *
   * @return the global {@code ExportComponent}.
   * @since 0.1.0
   */
  public static ExportComponent getExportComponent() {
    return metricsComponent.getExportComponent();
  }

  /**
   * Returns the global {@link MetricRegistry}.
   *
   * <p>This {@code MetricRegistry} is already added to the global {@link MetricProducerManager}.
   *
   * @return the global {@code MetricRegistry}.
   * @since 0.1.0
   */
  public static MetricRegistry getMetricRegistry() {
    return metricsComponent.getMetricRegistry();
  }

  private Metrics() {}
}
