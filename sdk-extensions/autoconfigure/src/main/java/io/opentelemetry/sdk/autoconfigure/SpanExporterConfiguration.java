/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;
import static java.util.stream.Collectors.toMap;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.StreamSupport;

final class SpanExporterConfiguration {

  private static final String EXPORTER_NONE = "none";

  // Visible for testing
  static Map<String, SpanExporter> configureSpanExporters(ConfigProperties config) {
    List<String> exporterNamesList = config.getCommaSeparatedValues("otel.traces.exporter");
    Set<String> exporterNames = new HashSet<>(exporterNamesList);
    if (exporterNamesList.size() != exporterNames.size()) {
      throw new ConfigurationException("otel.traces.exporter contains duplicates");
    }
    if (exporterNames.contains(EXPORTER_NONE) && exporterNames.size() > 1) {
      throw new ConfigurationException(
          "otel.traces.exporter contains " + EXPORTER_NONE + " along with other exporters");
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }

    if (exporterNames.contains(EXPORTER_NONE)) {
      return Collections.emptyMap();
    }

    Map<String, SpanExporter> spiExporters =
        StreamSupport.stream(
                ServiceLoader.load(ConfigurableSpanExporterProvider.class).spliterator(), false)
            .collect(
                toMap(
                    ConfigurableSpanExporterProvider::getName,
                    configurableSpanExporterProvider ->
                        configurableSpanExporterProvider.createExporter(config)));

    return exporterNames.stream()
        .collect(
            toMap(
                Function.identity(),
                exporterName -> configureExporter(exporterName, config, spiExporters)));
  }

  // Visible for testing
  static SpanExporter configureExporter(
      String name, ConfigProperties config, Map<String, SpanExporter> spiExporters) {
    switch (name) {
      case "otlp":
        return configureOtlpSpans(config);
      case "jaeger":
        return configureJaeger(config);
      case "zipkin":
        return configureZipkin(config);
      case "logging":
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.logging.LoggingSpanExporter",
            "Logging Trace Exporter",
            "opentelemetry-exporter-logging");
        return new LoggingSpanExporter();
      default:
        SpanExporter spiExporter = spiExporters.get(name);
        if (spiExporter == null) {
          throw new ConfigurationException("Unrecognized value for otel.traces.exporter: " + name);
        }
        return spiExporter;
    }
  }

  // Visible for testing
  static OtlpGrpcSpanExporter configureOtlpSpans(ConfigProperties config) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter",
        "OTLP Trace Exporter",
        "opentelemetry-exporter-otlp");
    OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();

    OtlpConfigUtil.configureOtlpExporterBuilder(
        DATA_TYPE_TRACES,
        config,
        builder::setEndpoint,
        builder::addHeader,
        builder::setTimeout,
        builder::setTrustedCertificates);

    return builder.build();
  }

  private static SpanExporter configureJaeger(ConfigProperties config) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter",
        "Jaeger gRPC Exporter",
        "opentelemetry-exporter-jaeger");
    JaegerGrpcSpanExporterBuilder builder = JaegerGrpcSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.jaeger.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Duration timeout = config.getDuration("otel.exporter.jaeger.timeout");
    if (timeout != null) {
      builder.setTimeout(timeout);
    }

    return builder.build();
  }

  private static SpanExporter configureZipkin(ConfigProperties config) {
    ClasspathUtil.checkClassExists(
        "io.opentelemetry.exporter.zipkin.ZipkinSpanExporter",
        "Zipkin Exporter",
        "opentelemetry-exporter-zipkin");
    ZipkinSpanExporterBuilder builder = ZipkinSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.zipkin.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Duration timeout = config.getDuration("otel.exporter.zipkin.timeout");
    if (timeout != null) {
      builder.setReadTimeout(timeout);
    }

    return builder.build();
  }

  private SpanExporterConfiguration() {}
}
