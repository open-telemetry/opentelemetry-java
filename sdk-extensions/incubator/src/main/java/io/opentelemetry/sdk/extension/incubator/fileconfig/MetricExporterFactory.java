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

final class MetricExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter,
        MetricExporter> {

  private static final MetricExporterFactory INSTANCE = new MetricExporterFactory();

  private MetricExporterFactory() {}

  static MetricExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public MetricExporter create(
      io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    OtlpMetric otlpModel = model.getOtlp();
    if (otlpModel != null) {
      return FileConfigUtil.addAndReturn(closeables, createOtlpExporter(otlpModel, spiHelper));
    }

    if (model.getConsole() != null) {
      return FileConfigUtil.addAndReturn(closeables, createConsoleExporter(spiHelper));
    }

    if (model.getPrometheus() != null) {
      throw new ConfigurationException("prometheus exporter not supported in this context");
    }

    // TODO(jack-berg): add support for generic SPI exporters
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized metric exporter(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    } else {
      throw new ConfigurationException("metric exporter must be set");
    }
  }

  private static MetricExporter createOtlpExporter(OtlpMetric model, SpiHelper spiHelper) {
    // Translate from file configuration scheme to environment variable scheme. This is ultimately
    // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
    // opentelemetry-exporter-otlp
    Map<String, String> properties = new HashMap<>();
    if (model.getProtocol() != null) {
      properties.put("otel.exporter.otlp.metrics.protocol", model.getProtocol());
    }
    if (model.getEndpoint() != null) {
      // NOTE: Set general otel.exporter.otlp.endpoint instead of signal specific
      // otel.exporter.otlp.metrics.endpoint to allow signal path (i.e. /v1/metrics) to be added
      // if not
      // present
      properties.put("otel.exporter.otlp.endpoint", model.getEndpoint());
    }
    if (model.getHeaders() != null) {
      properties.put(
          "otel.exporter.otlp.metrics.headers",
          model.getHeaders().getAdditionalProperties().entrySet().stream()
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(joining(",")));
    }
    if (model.getCompression() != null) {
      properties.put("otel.exporter.otlp.metrics.compression", model.getCompression());
    }
    if (model.getTimeout() != null) {
      properties.put("otel.exporter.otlp.metrics.timeout", Integer.toString(model.getTimeout()));
    }
    if (model.getCertificate() != null) {
      properties.put("otel.exporter.otlp.metrics.certificate", model.getCertificate());
    }
    if (model.getClientKey() != null) {
      properties.put("otel.exporter.otlp.metrics.client.key", model.getClientKey());
    }
    if (model.getClientCertificate() != null) {
      properties.put("otel.exporter.otlp.metrics.client.certificate", model.getClientCertificate());
    }
    if (model.getDefaultHistogramAggregation() != null) {
      properties.put(
          "otel.exporter.otlp.metrics.default.histogram.aggregation",
          model.getDefaultHistogramAggregation().value());
    }
    if (model.getTemporalityPreference() != null) {
      properties.put(
          "otel.exporter.otlp.metrics.temporality.preference", model.getTemporalityPreference());
    }

    ConfigProperties configProperties = DefaultConfigProperties.createFromMap(properties);
    return FileConfigUtil.assertNotNull(
        metricExporterSpiManager(configProperties, spiHelper).getByName("otlp"), "otlp exporter");
  }

  private static MetricExporter createConsoleExporter(SpiHelper spiHelper) {
    return FileConfigUtil.assertNotNull(
        metricExporterSpiManager(
                DefaultConfigProperties.createFromMap(Collections.emptyMap()), spiHelper)
            .getByName("logging"),
        "logging exporter");
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
