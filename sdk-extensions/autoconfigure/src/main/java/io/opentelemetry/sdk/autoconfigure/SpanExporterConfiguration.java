/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import javax.annotation.Nullable;

final class SpanExporterConfiguration {

  @Nullable
  static SpanExporter getExporter(String name, ConfigProperties config) {
    switch (name) {
      case "otlp":
      case "otlp_span":
        return configureOtlpSpans(config);
      case "jaeger":
        return configureJaeger(config);
      case "zipkin":
        return configureZipkin(config);
      case "logging":
        return new LoggingSpanExporter();
      default:
        return null;
    }
  }

  private static SpanExporter configureOtlpSpans(ConfigProperties config) {
    OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();

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

    return builder.build();
  }

  private static SpanExporter configureJaeger(ConfigProperties config) {
    JaegerGrpcSpanExporterBuilder builder = JaegerGrpcSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.jaeger.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    return builder.build();
  }

  private static SpanExporter configureZipkin(ConfigProperties config) {
    ZipkinSpanExporterBuilder builder = ZipkinSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.zipkin.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    return builder.build();
  }

  private SpanExporterConfiguration() {}
}
