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
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class LogRecordExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter,
        LogRecordExporter> {

  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter
              model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return LogRecordExporter.composite();
    }

    if (model.getOtlp() != null) {
      Otlp otlp = model.getOtlp();

      // Translate from file configuration scheme to environment variable scheme. This is ultimately
      // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
      // opentelemetry-exporter-otlp
      Map<String, String> properties = new HashMap<>();
      if (otlp.getProtocol() != null) {
        properties.put("otel.exporter.otlp.logs.protocol", otlp.getProtocol());
      }
      if (otlp.getEndpoint() != null) {
        properties.put("otel.exporter.otlp.logs.endpoint", otlp.getEndpoint());
      }
      if (otlp.getHeaders() != null) {
        properties.put(
            "otel.exporter.otlp.logs.headers",
            otlp.getHeaders().getAdditionalProperties().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(",")));
      }
      if (otlp.getCompression() != null) {
        properties.put("otel.exporter.otlp.logs.compression", otlp.getCompression());
      }
      if (otlp.getTimeout() != null) {
        properties.put("otel.exporter.otlp.logs.timeout", Integer.toString(otlp.getTimeout()));
      }
      if (otlp.getCertificate() != null) {
        properties.put("otel.exporter.otlp.logs.certificate", otlp.getCertificate());
      }
      if (otlp.getClientKey() != null) {
        properties.put("otel.exporter.otlp.logs.client.key", otlp.getClientKey());
      }
      if (otlp.getClientCertificate() != null) {
        properties.put("otel.exporter.otlp.logs.client.certificate", otlp.getClientCertificate());
      }

      // TODO(jack-berg): add method for creating from map
      ConfigProperties configProperties = DefaultConfigProperties.createForTest(properties);

      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              logRecordExporterSpiManager(configProperties, spiHelper).getByName("otlp"),
              "otlp exporter"));
    }

    // TODO(jack-berg): add support for generic SPI exporters
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized log record exporter(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return LogRecordExporter.composite();
  }

  private static NamedSpiManager<LogRecordExporter> logRecordExporterSpiManager(
      ConfigProperties config, SpiHelper spiHelper) {
    return spiHelper.loadConfigurable(
        ConfigurableLogRecordExporterProvider.class,
        ConfigurableLogRecordExporterProvider::getName,
        ConfigurableLogRecordExporterProvider::createExporter,
        config);
  }
}
