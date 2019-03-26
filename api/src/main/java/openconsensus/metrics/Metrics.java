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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import openconsensus.common.ExperimentalApi;
import openconsensus.internal.DefaultVisibilityForTesting;
import openconsensus.internal.Provider;
import openconsensus.metrics.export.ExportComponent;
import openconsensus.metrics.export.MetricProducerManager;

/**
 * Class for accessing the default {@link MetricsComponent}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public final class Metrics {
  private static final Logger logger = Logger.getLogger(Metrics.class.getName());
  private static final MetricsComponent metricsComponent =
      loadMetricsComponent(MetricsComponent.class.getClassLoader());

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

  // Any provider that may be used for MetricsComponent can be added here.
  @DefaultVisibilityForTesting
  static MetricsComponent loadMetricsComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "io.opencensus.impl.metrics.MetricsComponentImpl", /*initialize=*/ true, classLoader),
          MetricsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for MetricsComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "io.opencensus.impllite.metrics.MetricsComponentImplLite",
              /*initialize=*/ true,
              classLoader),
          MetricsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for MetricsComponent, now using default "
              + "implementation for MetricsComponent.",
          e);
    }
    return MetricsComponent.newNoopMetricsComponent();
  }

  private Metrics() {}
}
