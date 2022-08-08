/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_LOGS;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.logging.SystemOutLogExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogExporterProvider;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

class LogExporterConfiguration {

  private static final String EXPORTER_NONE = "none";

  // Visible for test
  static Map<String, LogExporter> configureLogExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super LogExporter, ConfigProperties, ? extends LogExporter>
          logExporterCustomizer) {
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

    NamedSpiManager<LogExporter> spiExportersManager =
        SpiUtil.loadConfigurable(
            ConfigurableLogExporterProvider.class,
            ConfigurableLogExporterProvider::getName,
            ConfigurableLogExporterProvider::createExporter,
            config,
            serviceClassLoader);

    Map<String, LogExporter> exportersByName = new HashMap<>();
    for (String name : exporterNames) {
      LogExporter logExporter = configureExporter(name, config, spiExportersManager, meterProvider);
      if (logExporter != null) {
        LogExporter customizedLogExporter = logExporterCustomizer.apply(logExporter, config);
        exportersByName.put(name, customizedLogExporter);
      }
    }

    return Collections.unmodifiableMap(exportersByName);
  }

  // Visible for testing
  @Nullable
  static LogExporter configureExporter(
      String name,
      ConfigProperties config,
      NamedSpiManager<LogExporter> spiExportersManager,
      MeterProvider meterProvider) {
    switch (name) {
      case "otlp":
        return configureOtlpLogs(config, meterProvider);
      case "logging":
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.logging.SystemOutLogExporter",
            "Logging Log Exporter",
            "opentelemetry-exporter-logging");
        return SystemOutLogExporter.create();
      default:
        LogExporter spiExporter = spiExportersManager.getByName(name);
        if (spiExporter == null) {
          throw new ConfigurationException("Unrecognized value for otel.logs.exporter: " + name);
        }
        return spiExporter;
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
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

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
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));
      builder.setMeterProvider(meterProvider);

      return builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP logs protocol: " + protocol);
    }
  }

  private LogExporterConfiguration() {}
}
