/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.Collections;

final class MetricExporterConfiguration {

  static boolean configureExporter(
      String name,
      ConfigProperties config,
      boolean metricsAlreadyRegistered,
      SdkMeterProvider meterProvider) {
    switch (name) {
      case "otlp":
      case "otlp_metrics":
        if (metricsAlreadyRegistered) {
          throw new IllegalStateException(
              "Multiple metrics exporters configured. Only one metrics exporter can be "
                  + "configured at a time.");
        }
        configureOtlpMetrics(config, meterProvider);
        return true;
      case "prometheus":
        if (metricsAlreadyRegistered) {
          throw new IllegalStateException(
              "Multiple metrics exporters configured. Only one metrics exporter can be "
                  + "configured at a time.");
        }
        configurePrometheusMetrics(config, meterProvider);
        return true;
      default:
        return false;
    }
  }

  private static void configureOtlpMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder();

    String endpoint = config.getString("otel.exporter.otlp.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    boolean insecure = config.getBoolean("otel.exporter.otlp.insecure");
    if (!insecure) {
      builder.setUseTls(true);
    }

    config.getCommaSeparatedMap("otel.exporter.otlp.headers").forEach(builder::addHeader);

    Long deadlineMs = config.getLong("otel.exporter.otlp.timeout");
    if (deadlineMs != null) {
      builder.setDeadlineMs(deadlineMs);
    }

    OtlpGrpcMetricExporter exporter = builder.build();

    IntervalMetricReader.Builder readerBuilder =
        IntervalMetricReader.builder()
            .setMetricProducers(Collections.singleton(meterProvider.getMetricProducer()))
            .setMetricExporter(exporter);
    Long exportIntervalMillis = config.getLong("otel.imr.export.interval");
    if (exportIntervalMillis != null) {
      readerBuilder.setExportIntervalMillis(exportIntervalMillis);
    }
    IntervalMetricReader reader = readerBuilder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(reader::shutdown));
  }

  private static void configurePrometheusMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    PrometheusCollector.builder()
        .setMetricProducer(meterProvider.getMetricProducer())
        .buildAndRegister();
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
