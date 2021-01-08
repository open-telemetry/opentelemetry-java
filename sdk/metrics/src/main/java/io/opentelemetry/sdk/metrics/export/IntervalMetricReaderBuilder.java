/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.api.internal.Utils;
import java.util.Collection;
import java.util.Map;

/** Builder for {@link IntervalMetricReader}. */
@SuppressWarnings("deprecation") // Remove after ConfigBuilder is deleted
public final class IntervalMetricReaderBuilder
    extends io.opentelemetry.sdk.common.export.ConfigBuilder<IntervalMetricReaderBuilder> {
  private final IntervalMetricReader.InternalState.Builder optionsBuilder;
  private static final String KEY_EXPORT_INTERVAL = "otel.imr.export.interval";

  IntervalMetricReaderBuilder(IntervalMetricReader.InternalState.Builder optionsBuilder) {
    this.optionsBuilder = optionsBuilder;
  }

  /**
   * Sets the export interval.
   *
   * @param exportIntervalMillis the export interval between pushes to the exporter.
   * @return this.
   */
  public IntervalMetricReaderBuilder setExportIntervalMillis(long exportIntervalMillis) {
    optionsBuilder.setExportIntervalMillis(exportIntervalMillis);
    return this;
  }

  /**
   * Sets the exporter to be called when export metrics.
   *
   * @param metricExporter the {@link MetricExporter} to be called when export metrics.
   * @return this.
   */
  public IntervalMetricReaderBuilder setMetricExporter(MetricExporter metricExporter) {
    optionsBuilder.setMetricExporter(metricExporter);
    return this;
  }

  /**
   * Sets a collection of {@link MetricProducer} from where the metrics should be read.
   *
   * @param metricProducers a collection of {@link MetricProducer} from where the metrics should be
   *     read.
   * @return this.
   */
  public IntervalMetricReaderBuilder setMetricProducers(
      Collection<MetricProducer> metricProducers) {
    optionsBuilder.setMetricProducers(metricProducers);
    return this;
  }

  /**
   * Builds a new {@link IntervalMetricReader} with current settings.
   *
   * @return a {@code IntervalMetricReader}.
   */
  public IntervalMetricReader build() {
    IntervalMetricReader.InternalState internalState = optionsBuilder.build();
    Utils.checkArgument(
        internalState.getExportIntervalMillis() > 0, "Export interval must be positive");

    return new IntervalMetricReader(internalState);
  }

  /**
   * Sets the configuration values from the given configuration map for only the available keys.
   *
   * @param configMap {@link Map} holding the configuration values.
   * @return this.
   */
  @Override
  protected IntervalMetricReaderBuilder fromConfigMap(
      Map<String, String> configMap, NamingConvention namingConvention) {
    configMap = namingConvention.normalize(configMap);
    Long value = getLongProperty(KEY_EXPORT_INTERVAL, configMap);
    if (value != null) {
      this.setExportIntervalMillis(value);
    }
    return this;
  }
}
