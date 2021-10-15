/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class SpanExporterConfiguration {

  private static final String EXPORTER_NONE = "none";

  // Visible for testing
  static Map<String, SpanExporter> configureSpanExporters(
      ConfigProperties config,
      Function<? super SpanExporter, ? extends SpanExporter> spanExporterCustomizer) {
    List<String> exporterNamesList = config.getList("otel.traces.exporter");
    Set<String> exporterNames = new HashSet<>(exporterNamesList);
    if (exporterNamesList.size() != exporterNames.size()) {
      String duplicates =
          exporterNamesList.stream()
              .collect(groupingBy(Function.identity(), counting()))
              .entrySet()
              .stream()
              .filter(entry -> entry.getValue() > 1)
              .map(Map.Entry::getKey)
              .collect(joining(",", "[", "]"));
      throw new ConfigurationException("otel.traces.exporter contains duplicates: " + duplicates);
    }
    if (exporterNames.contains(EXPORTER_NONE)) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.traces.exporter contains " + EXPORTER_NONE + " along with other exporters");
      }
      return Collections.emptyMap();
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }

    Map<String, SpanExporter> spiExporters =
        SpiUtil.loadConfigurable(
            ConfigurableSpanExporterProvider.class,
            exporterNamesList,
            ConfigurableSpanExporterProvider::getName,
            ConfigurableSpanExporterProvider::createExporter,
            config);

    return exporterNames.stream()
        .collect(
            toMap(
                Function.identity(),
                exporterName ->
                    spanExporterCustomizer.apply(
                        configureExporter(exporterName, config, spiExporters))));
  }

  // Visible for testing
  static SpanExporter configureExporter(
      String name, ConfigProperties config, Map<String, SpanExporter> spiExporters) {
    switch (name) {
      case "otlp":
        return configureOtlp(config);
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
  static SpanExporter configureOtlp(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_TRACES, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      ClasspathUtil.checkClassExists(
          "io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter",
          "OTLP HTTP Trace Exporter",
          "opentelemetry-exporter-otlp-http-trace");
      OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_TRACES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates);

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      ClasspathUtil.checkClassExists(
          "io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter",
          "OTLP gRPC Trace Exporter",
          "opentelemetry-exporter-otlp");
      OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_TRACES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates);

      return builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP traces protocol: " + protocol);
    }
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
