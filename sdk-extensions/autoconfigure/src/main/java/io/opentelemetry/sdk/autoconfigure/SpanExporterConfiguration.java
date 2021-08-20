/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

final class SpanExporterConfiguration {

  @Nullable
  static SpanExporter configureExporter(String name, ConfigProperties config) {
    Map<String, SpanExporter> spiExporters =
        StreamSupport.stream(
                ServiceLoader.load(ConfigurableSpanExporterProvider.class).spliterator(), false)
            .collect(
                Collectors.toMap(
                    ConfigurableSpanExporterProvider::getName,
                    configurableSpanExporterProvider ->
                        configurableSpanExporterProvider.createExporter(config)));

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
      case "none":
        return null;
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

    if (protocol.equals("http/protobuf")) {
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
          builder::setTimeout,
          builder::setTrustedCertificates);

      return builder.build();
    } else if (protocol.equals("grpc")) {
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
