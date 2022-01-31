/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.function.BiFunction;

final class MeterProviderConfiguration {

  static void configureMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {

    // Configure default exemplar filters.
    String exemplarFilter = config.getString("otel.metrics.exemplar.filter");
    if (exemplarFilter == null) {
      exemplarFilter = "with_sampled_trace";
    }
    switch (exemplarFilter) {
      case "none":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.neverSample());
        break;
      case "all":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.alwaysSample());
        break;
      case "with_sampled_trace":
      default:
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.sampleWithTraces());
        break;
    }

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName != null && !exporterName.equals("none")) {
      MetricExporterConfiguration.configureExporter(
          exporterName, config, serviceClassLoader, meterProviderBuilder, metricExporterCustomizer);
    }
  }

  private MeterProviderConfiguration() {}
}
