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
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class MetricExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter,
        MetricExporter> {

  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("NullAway") // Override superclass non-null response
  @Override
  @Nullable
  public MetricExporter create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return null;
    }

    OtlpMetric otlpModel = model.getOtlp();
    if (otlpModel != null) {
      // Translate from file configuration scheme to environment variable scheme. This is ultimately
      // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
      // opentelemetry-exporter-otlp
      Map<String, String> properties = new HashMap<>();
      if (otlpModel.getProtocol() != null) {
        properties.put("otel.exporter.otlp.metrics.protocol", otlpModel.getProtocol());
      }
      if (otlpModel.getEndpoint() != null) {
        // NOTE: Set general otel.exporter.otlp.endpoint instead of signal specific
        // otel.exporter.otlp.metrics.endpoint to allow signal path (i.e. /v1/metrics) to be added
        // if not
        // present
        properties.put("otel.exporter.otlp.endpoint", otlpModel.getEndpoint());
      }
      if (otlpModel.getHeaders() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.headers",
            otlpModel.getHeaders().getAdditionalProperties().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(",")));
      }
      if (otlpModel.getCompression() != null) {
        properties.put("otel.exporter.otlp.metrics.compression", otlpModel.getCompression());
      }
      if (otlpModel.getTimeout() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.timeout", Integer.toString(otlpModel.getTimeout()));
      }
      if (otlpModel.getCertificate() != null) {
        properties.put("otel.exporter.otlp.metrics.certificate", otlpModel.getCertificate());
      }
      if (otlpModel.getClientKey() != null) {
        properties.put("otel.exporter.otlp.metrics.client.key", otlpModel.getClientKey());
      }
      if (otlpModel.getClientCertificate() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.client.certificate", otlpModel.getClientCertificate());
      }
      if (otlpModel.getDefaultHistogramAggregation() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.default.histogram.aggregation",
            otlpModel.getDefaultHistogramAggregation().value());
      }
      if (otlpModel.getTemporalityPreference() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.temporality.preference",
            otlpModel.getTemporalityPreference());
      }

      // TODO(jack-berg): add method for creating from map
      ConfigProperties configProperties = DefaultConfigProperties.createForTest(properties);

      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              metricExporterSpiManager(configProperties, spiHelper).getByName("otlp"),
              "otlp exporter"));
    }

    if (model.getConsole() != null) {
      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              metricExporterSpiManager(
                      DefaultConfigProperties.createForTest(Collections.emptyMap()), spiHelper)
                  .getByName("logging"),
              "logging exporter"));
    }

    if (model.getPrometheus() != null) {
      throw new ConfigurationException("prometheus exporter not supported in this context");
    }

    // TODO(jack-berg): add support for generic SPI exporters
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized metric exporter(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return null;
  }

  private static NamedSpiManager<MetricExporter> metricExporterSpiManager(
      ConfigProperties config, SpiHelper spiHelper) {
    return spiHelper.loadConfigurable(
        ConfigurableMetricExporterProvider.class,
        ConfigurableMetricExporterProvider::getName,
        ConfigurableMetricExporterProvider::createExporter,
        config);
  }
}
