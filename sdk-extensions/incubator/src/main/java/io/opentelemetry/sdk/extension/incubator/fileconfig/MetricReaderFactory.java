/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigurableMetricReaderProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReaderBuilder;
import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class MetricReaderFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader,
        MetricReader> {

  private static final MetricReaderFactory INSTANCE = new MetricReaderFactory();

  private MetricReaderFactory() {}

  static MetricReaderFactory getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("NullAway") // Override superclass non-null response
  @Override
  @Nullable
  public MetricReader create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return null;
    }

    PeriodicMetricReader periodicModel = model.getPeriodic();
    if (periodicModel != null) {
      MetricExporter exporterModel = periodicModel.getExporter();
      if (exporterModel == null) {
        return null;
      }
      io.opentelemetry.sdk.metrics.export.MetricExporter metricExporter =
          MetricExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (metricExporter == null) {
        return null;
      }
      PeriodicMetricReaderBuilder builder =
          io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
              FileConfigUtil.addAndReturn(closeables, metricExporter));
      if (periodicModel.getInterval() != null) {
        builder.setInterval(Duration.ofMillis(periodicModel.getInterval()));
      }
      return FileConfigUtil.addAndReturn(closeables, builder.build());
    }

    PullMetricReader pullModel = model.getPull();
    if (pullModel != null) {
      MetricExporter exporterModel = pullModel.getExporter();
      if (exporterModel == null) {
        throw new ConfigurationException("exporter required for pull reader");
      }
      Prometheus prometheusModel = exporterModel.getPrometheus();
      if (prometheusModel != null) {

        // Translate from file configuration scheme to environment variable scheme. This is
        // ultimately
        // interpreted by PrometheusMetricReaderProvider, but we want to avoid the dependency on
        // opentelemetry-exporter-prometheus
        Map<String, String> properties = new HashMap<>();
        if (prometheusModel.getHost() != null) {
          properties.put("otel.exporter.prometheus.host", prometheusModel.getHost());
        }
        if (prometheusModel.getPort() != null) {
          properties.put(
              "otel.exporter.prometheus.port", String.valueOf(prometheusModel.getPort()));
        }

        // TODO(jack-berg): add method for creating from map
        ConfigProperties configProperties = DefaultConfigProperties.createForTest(properties);

        return FileConfigUtil.addAndReturn(
            closeables,
            FileConfigUtil.assertNotNull(
                metricReaderSpiManager(configProperties, spiHelper).getByName("prometheus"),
                "prometheus reader"));
      }

      if (exporterModel.getConsole() != null) {
        throw new ConfigurationException(
            "console exporter not currently supported in this context");
      }

      if (exporterModel.getOtlp() != null) {
        throw new ConfigurationException(
            "console exporter not currently supported in this context");
      }

      // TODO(jack-berg): add support for generic SPI exporters
      if (!exporterModel.getAdditionalProperties().isEmpty()) {
        throw new ConfigurationException(
            "Unrecognized metric exporter(s): "
                + exporterModel.getAdditionalProperties().keySet().stream()
                    .collect(joining(",", "[", "]")));
      }
    }

    return null;
  }

  private static NamedSpiManager<io.opentelemetry.sdk.metrics.export.MetricReader>
      metricReaderSpiManager(ConfigProperties config, SpiHelper spiHelper) {
    return spiHelper.loadConfigurable(
        ConfigurableMetricReaderProvider.class,
        ConfigurableMetricReaderProvider::getName,
        ConfigurableMetricReaderProvider::createMetricReader,
        config);
  }
}
