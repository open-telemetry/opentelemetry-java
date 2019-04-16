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

package openconsensus.opencensusshim.metrics.export;

import openconsensus.opencensusshim.common.ExperimentalApi;

/**
 * Class that holds the implementation instance for {@link MetricProducerManager}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class ExportComponent {
  /**
   * Returns the no-op implementation of the {@code ExportComponent}.
   *
   * @return the no-op implementation of the {@code ExportComponent}.
   * @since 0.1.0
   */
  public static ExportComponent newNoopExportComponent() {
    return new NoopExportComponent();
  }

  /**
   * Returns the global {@link MetricProducerManager} which can be used to register handlers to
   * export all the recorded metrics.
   *
   * @return the implementation of the {@code MetricExporter} or no-op if no implementation linked
   *     in the binary.
   * @since 0.1.0
   */
  public abstract MetricProducerManager getMetricProducerManager();

  private static final class NoopExportComponent extends ExportComponent {

    private static final MetricProducerManager METRIC_PRODUCER_MANAGER =
        MetricProducerManager.newNoopMetricProducerManager();

    @Override
    public MetricProducerManager getMetricProducerManager() {
      return METRIC_PRODUCER_MANAGER;
    }
  }
}
