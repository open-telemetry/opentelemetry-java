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
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class SpanExporterFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter,
        SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return SpanExporter.composite();
    }

    if (model.getOtlp() != null) {
      Otlp otlp = model.getOtlp();

      // Translate from file configuration scheme to environment variable scheme. This is ultimately
      // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
      // opentelemetry-exporter-otlp
      Map<String, String> properties = new HashMap<>();
      if (otlp.getProtocol() != null) {
        properties.put("otel.exporter.otlp.traces.protocol", otlp.getProtocol());
      }
      if (otlp.getEndpoint() != null) {
        // NOTE: Set general otel.exporter.otlp.endpoint instead of signal specific
        // otel.exporter.otlp.traces.endpoint to allow signal path (i.e. /v1/traces) to be added if
        // not present
        properties.put("otel.exporter.otlp.endpoint", otlp.getEndpoint());
      }
      if (otlp.getHeaders() != null) {
        properties.put(
            "otel.exporter.otlp.traces.headers",
            otlp.getHeaders().getAdditionalProperties().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(",")));
      }
      if (otlp.getCompression() != null) {
        properties.put("otel.exporter.otlp.traces.compression", otlp.getCompression());
      }
      if (otlp.getTimeout() != null) {
        properties.put("otel.exporter.otlp.traces.timeout", Integer.toString(otlp.getTimeout()));
      }
      if (otlp.getCertificate() != null) {
        properties.put("otel.exporter.otlp.traces.certificate", otlp.getCertificate());
      }
      if (otlp.getClientKey() != null) {
        properties.put("otel.exporter.otlp.traces.client.key", otlp.getClientKey());
      }
      if (otlp.getClientCertificate() != null) {
        properties.put("otel.exporter.otlp.traces.client.certificate", otlp.getClientCertificate());
      }

      // TODO(jack-berg): add method for creating from map
      ConfigProperties configProperties = DefaultConfigProperties.createForTest(properties);

      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              spanExporterSpiManager(configProperties, spiHelper).getByName("otlp"),
              "otlp exporter"));
    }

    if (model.getConsole() != null) {
      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              spanExporterSpiManager(
                      DefaultConfigProperties.createForTest(Collections.emptyMap()), spiHelper)
                  .getByName("logging"),
              "logging exporter"));
    }

    // TODO(jack-berg): add support for generic SPI exporters
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized span exporter(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return SpanExporter.composite();
  }

  private static NamedSpiManager<SpanExporter> spanExporterSpiManager(
      ConfigProperties config, SpiHelper spiHelper) {
    return spiHelper.loadConfigurable(
        ConfigurableSpanExporterProvider.class,
        ConfigurableSpanExporterProvider::getName,
        ConfigurableSpanExporterProvider::createExporter,
        config);
  }
}
