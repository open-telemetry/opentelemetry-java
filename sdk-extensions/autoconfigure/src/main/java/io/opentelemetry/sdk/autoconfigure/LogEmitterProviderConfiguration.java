/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_LOGS;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.okhttp.OkHttpExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.SdkLogEmitterProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

final class LogEmitterProviderConfiguration {

  private static final String EXPORTER_NONE = "none";

  static SdkLogEmitterProvider configureLogEmitterProvider(
      Resource resource, ConfigProperties config, MeterProvider meterProvider) {
    SdkLogEmitterProviderBuilder builder = SdkLogEmitterProvider.builder().setResource(resource);

    Map<String, LogExporter> exportersByName = configureLogExporters(config, meterProvider);

    exportersByName.forEach(
        (name, exporter) -> {
          BatchLogProcessor batchLogProcessor = BatchLogProcessor.builder(exporter).build();
          builder.addLogProcessor(batchLogProcessor);
        });

    SdkLogEmitterProvider logEmitterProvider = builder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(logEmitterProvider::close));
    return logEmitterProvider;
  }

  // Visible for test
  static Map<String, LogExporter> configureLogExporters(
      ConfigProperties config, MeterProvider meterProvider) {
    Set<String> exporterNames = DefaultConfigProperties.getSet(config, "otel.logs.exporter");

    // Default to no exporter
    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton(EXPORTER_NONE);
    }

    if (exporterNames.contains(EXPORTER_NONE)) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.logs.exporter contains " + EXPORTER_NONE + " along with other exporters");
      }
      return Collections.emptyMap();
    }

    Map<String, LogExporter> exportersByName = new HashMap<>();
    for (String name : exporterNames) {
      LogExporter logExporter = configureExporter(name, config, meterProvider);
      if (logExporter != null) {
        exportersByName.put(name, logExporter);
      }
    }

    return Collections.unmodifiableMap(exportersByName);
  }

  // Visible for testing
  @Nullable
  static LogExporter configureExporter(
      String name, ConfigProperties config, MeterProvider meterProvider) {
    switch (name) {
      case "otlp":
        return configureOtlpLogs(config, meterProvider);
      default:
        throw new ConfigurationException("Unrecognized value for otel.logs.exporter: " + name);
    }
  }

  // Visible for testing
  @Nullable
  static LogExporter configureOtlpLogs(ConfigProperties config, MeterProvider meterProvider) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_LOGS, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      try {
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporter",
            "OTLP HTTP Log Exporter",
            "opentelemetry-exporter-otlp-http-logs");
      } catch (ConfigurationException e) {
        // Squash this for now until logs are stable
        return null;
      }
      OtlpHttpLogExporterBuilder builder = OtlpHttpLogExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_LOGS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          retryPolicy ->
              OkHttpExporterBuilder.getDelegateBuilder(OtlpHttpLogExporterBuilder.class, builder)
                  .setRetryPolicy(retryPolicy));

      builder.setMeterProvider(meterProvider);

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      try {
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter",
            "OTLP gRPC Log Exporter",
            "opentelemetry-exporter-otlp-logs");
      } catch (ConfigurationException e) {
        // Squash this for now until logs are stable
        return null;
      }
      OtlpGrpcLogExporterBuilder builder = OtlpGrpcLogExporter.builder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_LOGS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          retryPolicy ->
              DefaultGrpcExporterBuilder.getDelegateBuilder(
                      OtlpGrpcLogExporterBuilder.class, builder)
                  .setRetryPolicy(retryPolicy));
      builder.setMeterProvider(meterProvider);

      return builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP logs protocol: " + protocol);
    }
  }

  private LogEmitterProviderConfiguration() {}
}
