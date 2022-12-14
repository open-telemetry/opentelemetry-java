/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

final class MeterProviderConfiguration {

  static void configureMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {

    // Configure default exemplar filters.
    String exemplarFilter = config.getString("otel.metrics.exemplar.filter", "with_sampled_trace");
    switch (exemplarFilter) {
      case "none":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.neverSample());
        break;
      case "all":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.alwaysSample());
        break;
      case "with_sampled_trace":
      default:
        SdkMeterProviderUtil.setExemplarFilter(
            meterProviderBuilder, ExemplarFilter.sampleWithTraces());
        break;
    }

    configureMetricReaders(config, serviceClassLoader, metricExporterCustomizer)
        .forEach(meterProviderBuilder::registerMetricReader);
  }

  static List<MetricReader> configureMetricReaders(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {
    Set<String> exporterNames = DefaultConfigProperties.getSet(config, "otel.metrics.exporter");
    if (exporterNames.contains("none")) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.metrics.exporter contains none along with other exporters");
      }
      return Collections.emptyList();
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }
    return exporterNames.stream()
        .map(
            exporterName ->
                MetricExporterConfiguration.configureExporter(
                    exporterName, config, serviceClassLoader, metricExporterCustomizer))
        .collect(Collectors.toList());
  }

  private MeterProviderConfiguration() {}
}
