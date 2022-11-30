/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

final class MetricExporterConfiguration {

  private static final Duration DEFAULT_EXPORT_INTERVAL = Duration.ofMinutes(1);
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("otlp", "opentelemetry-exporter-otlp");
  }

  static MetricReader configureReader(
      String name,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {
    if (name.equals("prometheus")) {
      return configurePrometheusMetricReader(config);
    }

    NamedSpiManager<MetricExporter> spiExportersManager =
        metricExporterSpiManager(config, serviceClassLoader);

    MetricExporter metricExporter = configureExporter(name, spiExportersManager);
    metricExporter = metricExporterCustomizer.apply(metricExporter, config);
    return configurePeriodicMetricReader(config, metricExporter);
  }

  // Visible for testing
  static NamedSpiManager<MetricExporter> metricExporterSpiManager(
      ConfigProperties config, ClassLoader serviceClassLoader) {
    return SpiUtil.loadConfigurable(
        ConfigurableMetricExporterProvider.class,
        ConfigurableMetricExporterProvider::getName,
        ConfigurableMetricExporterProvider::createExporter,
        config,
        serviceClassLoader);
  }

  // Visible for testing.
  static MetricExporter configureExporter(
      String name, NamedSpiManager<MetricExporter> spiExportersManager) {
    MetricExporter metricExporter = spiExportersManager.getByName(name);
    if (metricExporter == null) {
      String artifactId = EXPORTER_ARTIFACT_ID_BY_NAME.get(name);
      if (artifactId != null) {
        throw new ConfigurationException(
            "otel.metrics.exporter set to \""
                + name
                + "\" but "
                + artifactId
                + " not found on classpath. Make sure to add it as a dependency.");
      }
      throw new ConfigurationException("Unrecognized value for otel.metrics.exporter: " + name);
    }
    return metricExporter;
  }

  private static PeriodicMetricReader configurePeriodicMetricReader(
      ConfigProperties config, MetricExporter exporter) {

    return PeriodicMetricReader.builder(exporter)
        .setInterval(config.getDuration("otel.metric.export.interval", DEFAULT_EXPORT_INTERVAL))
        .build();
  }

  private static PrometheusHttpServer configurePrometheusMetricReader(ConfigProperties config) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.prometheus.PrometheusHttpServer",
        "Prometheus Metrics Server",
        "opentelemetry-exporter-prometheus");
    PrometheusHttpServerBuilder prom = PrometheusHttpServer.builder();

    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port != null) {
      prom.setPort(port);
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host != null) {
      prom.setHost(host);
    }
    return prom.build();
  }

  private MetricExporterConfiguration() {}
}
