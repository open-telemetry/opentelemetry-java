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

    if (model.getOtlp() != null) {
      OtlpMetric otlp = model.getOtlp();

      // Translate from file configuration scheme to environment variable scheme. This is ultimately
      // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
      // opentelemetry-exporter-otlp
      Map<String, String> properties = new HashMap<>();
      if (otlp.getProtocol() != null) {
        properties.put("otel.exporter.otlp.metrics.protocol", otlp.getProtocol());
      }
      if (otlp.getEndpoint() != null) {
        // NOTE: Set general otel.exporter.otlp.endpoint instead of signal specific
        // otel.exporter.otlp.metrics.endpoint to allow signal path (i.e. /v1/metrics) to be added
        // if not
        // present
        properties.put("otel.exporter.otlp.endpoint", otlp.getEndpoint());
      }
      if (otlp.getHeaders() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.headers",
            otlp.getHeaders().getAdditionalProperties().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(",")));
      }
      if (otlp.getCompression() != null) {
        properties.put("otel.exporter.otlp.metrics.compression", otlp.getCompression());
      }
      if (otlp.getTimeout() != null) {
        properties.put("otel.exporter.otlp.metrics.timeout", Integer.toString(otlp.getTimeout()));
      }
      if (otlp.getCertificate() != null) {
        properties.put("otel.exporter.otlp.metrics.certificate", otlp.getCertificate());
      }
      if (otlp.getClientKey() != null) {
        properties.put("otel.exporter.otlp.metrics.client.key", otlp.getClientKey());
      }
      if (otlp.getClientCertificate() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.client.certificate", otlp.getClientCertificate());
      }
      if (otlp.getDefaultHistogramAggregation() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.default.histogram.aggregation",
            otlp.getDefaultHistogramAggregation().value());
      }
      if (otlp.getTemporalityPreference() != null) {
        properties.put(
            "otel.exporter.otlp.metrics.temporality.preference", otlp.getTemporalityPreference());
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
