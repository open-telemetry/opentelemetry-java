/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_METRICS;

import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

final class MetricExporterConfiguration {
  static void configureExporter(
      String name, ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    switch (name) {
      case "otlp":
        configureOtlpMetrics(config, sdkMeterProviderBuilder);
        return;
      case "prometheus":
        configurePrometheusMetrics(config, sdkMeterProviderBuilder);
        return;
      case "logging":
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.logging.LoggingMetricExporter",
            "Logging Metrics Exporter",
            "opentelemetry-exporter-logging");
        configureLoggingMetrics(config, sdkMeterProviderBuilder);
        return;
      case "none":
        return;
      default:
        MetricExporter spiExporter = configureSpiExporter(name, config);
        if (spiExporter == null) {
          throw new ConfigurationException("Unrecognized value for otel.metrics.exporter: " + name);
        }
        configureIntervalMetricReader(config, sdkMeterProviderBuilder, spiExporter);
        return;
    }
  }

  // Visible for testing.
  @Nullable
  static MetricExporter configureSpiExporter(String name, ConfigProperties config) {
    Map<String, MetricExporter> spiExporters =
        SpiUtil.loadConfigurable(
            ConfigurableMetricExporterProvider.class,
            Collections.singletonList(name),
            ConfigurableMetricExporterProvider::getName,
            ConfigurableMetricExporterProvider::createExporter,
            config);
    return spiExporters.get(name);
  }

  private static void configureLoggingMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    configureIntervalMetricReader(config, sdkMeterProviderBuilder, new LoggingMetricExporter());
  }

  // Visible for testing
  @Nullable
  static MetricExporter configureOtlpMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_METRICS, config);

    MetricExporter exporter;
    if (protocol.equals("http/protobuf")) {
      try {
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter",
            "OTLP HTTP Metrics Exporter",
            "opentelemetry-exporter-otlp-http-metrics");
      } catch (ConfigurationException e) {
        // Squash this for now, until metrics are stable
        return null;
      }
      OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates);

      exporter = builder.build();
    } else if (protocol.equals("grpc")) {
      try {
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter",
            "OTLP gRPC Metrics Exporter",
            "opentelemetry-exporter-otlp-metrics");
      } catch (ConfigurationException e) {
        // Squash this for now, until metrics are stable and included in the `exporter-otlp`
        // artifact
        // by default,
        return null;
      }
      OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates);

      exporter = builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
    }

    configureIntervalMetricReader(config, sdkMeterProviderBuilder, exporter);

    return exporter;
  }

  private static void configureIntervalMetricReader(
      ConfigProperties config,
      SdkMeterProviderBuilder sdkMeterProviderBuilder,
      MetricExporter exporter) {

    Duration exportInterval = config.getDuration("otel.imr.export.interval");
    if (exportInterval == null) {
      exportInterval = Duration.ofMinutes(1);
    }
    // Register the reader (which starts it), and use a global shutdown hook rather than
    // interval reader specific.
    sdkMeterProviderBuilder.registerMetricReader(
        new PeriodicMetricReader.Factory(exporter, exportInterval));
  }

  private static void configurePrometheusMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.prometheus.PrometheusCollector",
        "Prometheus Metrics Server",
        "opentelemetry-exporter-prometheus");
    sdkMeterProviderBuilder.registerMetricReader(PrometheusCollector.create());
    // TODO: Can we move this portion into the PrometheusCollector so shutdown on SdkMeterProvider
    // will collapse prometheus too?
    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port == null) {
      port = 9464;
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host == null) {
      host = "0.0.0.0";
    }
    final HTTPServer server;
    try {
      server = new HTTPServer(host, port, true);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create Prometheus server", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(server::close));
  }

  private MetricExporterConfiguration() {}
}
