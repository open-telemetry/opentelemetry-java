/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

final class MeterProviderConfiguration {

  static void configureMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder,
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer,
      List<Closeable> closeables) {

    // Configure default exemplar filters.
    String exemplarFilter =
        config.getString("otel.metrics.exemplar.filter", "trace_based").toLowerCase(Locale.ROOT);
    switch (exemplarFilter) {
      case "always_off":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.alwaysOff());
        break;
      case "always_on":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.alwaysOn());
        break;
      case "trace_based":
      default:
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.traceBased());
        break;
    }

    int cardinalityLimit =
        config.getInt(
            "otel.experimental.metrics.cardinality.limit", MetricStorage.DEFAULT_MAX_CARDINALITY);
    if (cardinalityLimit < 1) {
      throw new ConfigurationException("otel.experimental.metrics.cardinality.limit must be >= 1");
    }

    configureMetricReaders(config, spiHelper, metricExporterCustomizer, closeables)
        .forEach(
            reader ->
                SdkMeterProviderUtil.registerMetricReaderWithCardinalitySelector(
                    meterProviderBuilder, reader, instrumentType -> cardinalityLimit));
  }

  static List<MetricReader> configureMetricReaders(
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer,
      List<Closeable> closeables) {
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
                MetricExporterConfiguration.configureReader(
                    exporterName, config, spiHelper, metricExporterCustomizer, closeables))
        .collect(Collectors.toList());
  }

  private MeterProviderConfiguration() {}
}
