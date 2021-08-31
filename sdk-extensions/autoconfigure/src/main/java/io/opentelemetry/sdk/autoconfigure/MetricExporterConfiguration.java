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
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReaderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

final class MetricExporterConfiguration {
  static void configureExporter(
      String name, ConfigProperties config, SdkMeterProvider meterProvider) {
    switch (name) {
      case "otlp":
        configureOtlpMetrics(config, meterProvider);
        return;
      case "prometheus":
        configurePrometheusMetrics(config, meterProvider);
        return;
      case "logging":
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.logging.LoggingMetricExporter",
            "Logging Metrics Exporter",
            "opentelemetry-exporter-logging");
        configureLoggingMetrics(config, meterProvider);
        return;
      case "none":
        return;
      default:
        MetricExporter spiExporter = configureSpiExporter(name, config);
        if (spiExporter == null) {
          throw new ConfigurationException("Unrecognized value for otel.metrics.exporter: " + name);
        }
        configureIntervalMetricReader(config, meterProvider, spiExporter);
        return;
    }
  }

  // Visible for testing.
  @Nullable
  static MetricExporter configureSpiExporter(String name, ConfigProperties config) {
    Map<String, MetricExporter> spiExporters =
        StreamSupport.stream(
                ServiceLoader.load(ConfigurableMetricExporterProvider.class).spliterator(), false)
            .collect(
                Collectors.toMap(
                    ConfigurableMetricExporterProvider::getName,
                    configurableSpanExporterProvider ->
                        configurableSpanExporterProvider.createExporter(config)));
    return spiExporters.get(name);
  }

  private static void configureLoggingMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    MetricExporter exporter = new LoggingMetricExporter();
    configureIntervalMetricReader(config, meterProvider, exporter);
  }

  // Visible for testing
  @Nullable
  static MetricExporter configureOtlpMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
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
          builder::setTimeout,
          builder::setTrustedCertificates);

      exporter = builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
    }

    configureIntervalMetricReader(config, meterProvider, exporter);

    return exporter;
  }

  private static void configureIntervalMetricReader(
      ConfigProperties config, SdkMeterProvider meterProvider, MetricExporter exporter) {
    IntervalMetricReaderBuilder readerBuilder =
        IntervalMetricReader.builder()
            .setMetricProducers(Collections.singleton(meterProvider))
            .setMetricExporter(exporter);
    Duration exportInterval = config.getDuration("otel.imr.export.interval");
    if (exportInterval != null) {
      readerBuilder.setExportIntervalMillis(exportInterval.toMillis());
    }
    IntervalMetricReader reader = readerBuilder.build().startAndRegisterGlobal();
    Runtime.getRuntime().addShutdownHook(new Thread(reader::shutdown));
  }

  private static void configurePrometheusMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.prometheus.PrometheusCollector",
        "Prometheus Metrics Server",
        "opentelemetry-exporter-prometheus");
    PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();
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
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }

  private MetricExporterConfiguration() {}
}
