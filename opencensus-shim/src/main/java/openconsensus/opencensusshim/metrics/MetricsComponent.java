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

package openconsensus.opencensusshim.metrics;

import openconsensus.opencensusshim.common.ExperimentalApi;
import openconsensus.opencensusshim.metrics.export.ExportComponent;

/**
 * Class that holds the implementation instance for {@link ExportComponent}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class MetricsComponent {

  /**
   * Returns the {@link ExportComponent} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link ExportComponent} implementation.
   * @since 0.1.0
   */
  public abstract ExportComponent getExportComponent();

  /**
   * Returns the {@link MetricRegistry} with the provided implementation.
   *
   * @return the {@link MetricRegistry} implementation.
   * @since 0.1.0
   */
  public abstract MetricRegistry getMetricRegistry();

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   */
  static MetricsComponent newNoopMetricsComponent() {
    return new NoopMetricsComponent();
  }

  private static final class NoopMetricsComponent extends MetricsComponent {
    private static final ExportComponent EXPORT_COMPONENT =
        ExportComponent.newNoopExportComponent();
    private static final MetricRegistry METRIC_REGISTRY = MetricRegistry.newNoopMetricRegistry();

    @Override
    public ExportComponent getExportComponent() {
      return EXPORT_COMPONENT;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
      return METRIC_REGISTRY;
    }
  }
}
