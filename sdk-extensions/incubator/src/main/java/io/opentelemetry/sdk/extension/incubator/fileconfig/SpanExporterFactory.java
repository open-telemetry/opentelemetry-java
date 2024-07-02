/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Zipkin;
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

    Otlp otlpModel = model.getOtlp();
    if (otlpModel != null) {
      return FileConfigUtil.addAndReturn(closeables, createOtlpExporter(otlpModel, spiHelper));
    }

    if (model.getConsole() != null) {
      return FileConfigUtil.addAndReturn(closeables, createConsoleExporter(spiHelper));
    }

    Zipkin zipkinModel = model.getZipkin();
    if (zipkinModel != null) {
      return FileConfigUtil.addAndReturn(closeables, createZipkinExporter(zipkinModel, spiHelper));
    }

    // TODO(jack-berg): add support for generic SPI exporters
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new StructuredConfigException(
          "Unrecognized span exporter(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return SpanExporter.composite();
  }

  private static SpanExporter createOtlpExporter(Otlp model, SpiHelper spiHelper) {
    // Translate from file configuration scheme to environment variable scheme. This is ultimately
    // interpreted by Otlp*ExporterProviders, but we want to avoid the dependency on
    // opentelemetry-exporter-otlp
    Map<String, String> properties = new HashMap<>();
    if (model.getProtocol() != null) {
      properties.put("otel.exporter.otlp.traces.protocol", model.getProtocol());
    }
    if (model.getEndpoint() != null) {
      // NOTE: Set general otel.exporter.otlp.endpoint instead of signal specific
      // otel.exporter.otlp.traces.endpoint to allow signal path (i.e. /v1/traces) to be added if
      // not present
      properties.put("otel.exporter.otlp.endpoint", model.getEndpoint());
    }
    if (model.getHeaders() != null) {
      properties.put(
          "otel.exporter.otlp.traces.headers",
          model.getHeaders().getAdditionalProperties().entrySet().stream()
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(joining(",")));
    }
    if (model.getCompression() != null) {
      properties.put("otel.exporter.otlp.traces.compression", model.getCompression());
    }
    if (model.getTimeout() != null) {
      properties.put("otel.exporter.otlp.traces.timeout", Integer.toString(model.getTimeout()));
    }
    if (model.getCertificate() != null) {
      properties.put("otel.exporter.otlp.traces.certificate", model.getCertificate());
    }
    if (model.getClientKey() != null) {
      properties.put("otel.exporter.otlp.traces.client.key", model.getClientKey());
    }
    if (model.getClientCertificate() != null) {
      properties.put("otel.exporter.otlp.traces.client.certificate", model.getClientCertificate());
    }

    ConfigProperties configProperties = DefaultConfigProperties.createFromMap(properties);
    return FileConfigUtil.assertNotNull(
        spanExporterSpiManager(configProperties, spiHelper).getByName("otlp"), "otlp exporter");
  }

  private static SpanExporter createConsoleExporter(SpiHelper spiHelper) {
    return FileConfigUtil.assertNotNull(
        spanExporterSpiManager(
                DefaultConfigProperties.createFromMap(Collections.emptyMap()), spiHelper)
            .getByName("logging"),
        "logging exporter");
  }

  private static SpanExporter createZipkinExporter(Zipkin model, SpiHelper spiHelper) {
    // Translate from file configuration scheme to environment variable scheme. This is ultimately
    // interpreted by ZipkinSpanExporterProvider, but we want to avoid the dependency on
    // opentelemetry-exporter-zipkin
    Map<String, String> properties = new HashMap<>();
    if (model.getEndpoint() != null) {
      properties.put("otel.exporter.zipkin.endpoint", model.getEndpoint());
    }
    if (model.getTimeout() != null) {
      properties.put("otel.exporter.zipkin.timeout", Integer.toString(model.getTimeout()));
    }

    ConfigProperties configProperties = DefaultConfigProperties.createFromMap(properties);
    return FileConfigUtil.assertNotNull(
        spanExporterSpiManager(configProperties, spiHelper).getByName("zipkin"), "zipkin exporter");
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
