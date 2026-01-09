/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class MeterProviderConfiguration {

  private static final Logger logger = Logger.getLogger(MeterProviderConfiguration.class.getName());

  static void configureMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder,
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super MetricReader, ConfigProperties, ? extends MetricReader>
          metricReaderCustomizer,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer,
      List<Closeable> closeables) {

    // Configure default exemplar filters.
    String exemplarFilter =
        config.getString("otel.metrics.exemplar.filter", "trace_based").toLowerCase(Locale.ROOT);
    switch (exemplarFilter) {
      case "always_off":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.alwaysOff());
        break;
      case "always_on":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.alwaysOn());
        break;
      case "trace_based":
      default:
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.traceBased());
        break;
    }

    Integer cardinalityLimit = config.getInt("otel.java.metrics.cardinality.limit");
    if (cardinalityLimit == null) {
      cardinalityLimit = config.getInt("otel.experimental.metrics.cardinality.limit");
      if (cardinalityLimit != null) {
        logger.warning(
            "otel.experimental.metrics.cardinality.limit is deprecated and will be removed after 1.51.0 release. Please use otel.java.metrics.cardinality.limit instead.");
      }
    }
    if (cardinalityLimit != null && cardinalityLimit < 1) {
      throw new ConfigurationException("otel.java.metrics.cardinality.limit must be >= 1");
    }
    int resolvedCardinalityLimit =
        cardinalityLimit == null ? MetricStorage.DEFAULT_MAX_CARDINALITY : cardinalityLimit;

    configureMetricReaders(
            config, spiHelper, metricReaderCustomizer, metricExporterCustomizer, closeables)
        .forEach(
            reader ->
                meterProviderBuilder.registerMetricReader(
                    reader, instrumentType -> resolvedCardinalityLimit));
  }

  static List<MetricReader> configureMetricReaders(
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super MetricReader, ConfigProperties, ? extends MetricReader>
          metricReaderCustomizer,
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
                    exporterName,
                    config,
                    spiHelper,
                    metricReaderCustomizer,
                    metricExporterCustomizer,
                    closeables))
        .collect(Collectors.toList());
  }

  private MeterProviderConfiguration() {}
}
