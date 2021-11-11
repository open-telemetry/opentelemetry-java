/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

final class MetricExporterConfiguration {
  static void configureExporter(
      String name,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      SdkMeterProviderBuilder sdkMeterProviderBuilder) {
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
      default:
        MetricExporter spiExporter = configureSpiExporter(name, config, serviceClassLoader);
        if (spiExporter == null) {
          throw new ConfigurationException("Unrecognized value for otel.metrics.exporter: " + name);
        }
        configurePeriodicMetricReader(config, sdkMeterProviderBuilder, spiExporter);
        return;
    }
  }

  // Visible for testing.
  @Nullable
  static MetricExporter configureSpiExporter(
      String name, ConfigProperties config, ClassLoader serviceClassLoader) {
    Map<String, MetricExporter> spiExporters =
        SpiUtil.loadConfigurable(
            ConfigurableMetricExporterProvider.class,
            Collections.singletonList(name),
            ConfigurableMetricExporterProvider::getName,
            ConfigurableMetricExporterProvider::createExporter,
            config,
            serviceClassLoader);
    return spiExporters.get(name);
  }

  private static void configureLoggingMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    configurePeriodicMetricReader(config, sdkMeterProviderBuilder, new LoggingMetricExporter());
  }

  // Visible for testing
  @Nullable
  static MetricExporter configureOtlpMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_METRICS, config);

    MetricExporter exporter;
    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
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
          builder::setTrustedCertificates,
          (unused) -> {});

      exporter = builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
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
          builder::setTrustedCertificates,
          retryPolicy ->
              DefaultGrpcExporterBuilder.getDelegateBuilder(
                      OtlpGrpcMetricExporterBuilder.class, builder)
                  .addRetryPolicy(retryPolicy));

      exporter = builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
    }

    configurePeriodicMetricReader(config, sdkMeterProviderBuilder, exporter);

    return exporter;
  }

  private static void configurePeriodicMetricReader(
      ConfigProperties config,
      SdkMeterProviderBuilder sdkMeterProviderBuilder,
      MetricExporter exporter) {

    Duration exportInterval = config.getDuration("otel.metric.export.interval");
    if (exportInterval == null) {
      exportInterval = config.getDuration("otel.imr.export.interval");
    }
    if (exportInterval == null) {
      exportInterval = Duration.ofMinutes(1);
    }
    // Register the reader (which will start when SDK is built).
    // This will shutdown when the SDK is shutdown.
    sdkMeterProviderBuilder.registerMetricReader(
        PeriodicMetricReader.builder(exporter)
            .setInterval(exportInterval)
            .newMetricReaderFactory());
  }

  private static void configurePrometheusMetrics(
      ConfigProperties config, SdkMeterProviderBuilder sdkMeterProviderBuilder) {
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

    sdkMeterProviderBuilder.registerMetricReader(prom.newMetricReaderFactory());
  }

  private MetricExporterConfiguration() {}
}
