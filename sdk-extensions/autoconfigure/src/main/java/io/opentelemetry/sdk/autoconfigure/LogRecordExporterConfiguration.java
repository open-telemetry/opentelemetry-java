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
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

class LogRecordExporterConfiguration {

  private static final String EXPORTER_NONE = "none";
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
  }

  // Visible for test
  static Map<String, LogRecordExporter> configureLogRecordExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          logRecordExporterCustomizer) {
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

    NamedSpiManager<LogRecordExporter> spiExportersManager =
        logRecordExporterSpiManager(config, serviceClassLoader);

    Map<String, LogRecordExporter> exportersByName = new HashMap<>();
    for (String name : exporterNames) {
      LogRecordExporter logRecordExporter =
          configureExporter(name, config, spiExportersManager, meterProvider);
      if (logRecordExporter != null) {
        LogRecordExporter customizedLogRecordExporter =
            logRecordExporterCustomizer.apply(logRecordExporter, config);
        exportersByName.put(name, customizedLogRecordExporter);
      }
    }

    return Collections.unmodifiableMap(exportersByName);
  }

  // Visible for testing
  static NamedSpiManager<LogRecordExporter> logRecordExporterSpiManager(
      ConfigProperties config, ClassLoader serviceClassLoader) {
    return SpiUtil.loadConfigurable(
        ConfigurableLogRecordExporterProvider.class,
        ConfigurableLogRecordExporterProvider::getName,
        ConfigurableLogRecordExporterProvider::createExporter,
        config,
        serviceClassLoader);
  }

  // Visible for testing
  @Nullable
  static LogRecordExporter configureExporter(
      String name,
      ConfigProperties config,
      NamedSpiManager<LogRecordExporter> spiExportersManager,
      MeterProvider meterProvider) {
    switch (name) {
      case "otlp":
        return configureOtlpLogs(config, meterProvider);
      default:
        LogRecordExporter spiExporter = spiExportersManager.getByName(name);
        if (spiExporter == null) {
          String artifactId = EXPORTER_ARTIFACT_ID_BY_NAME.get(name);
          if (artifactId != null) {
            throw new ConfigurationException(
                "otel.logs.exporter set to \""
                    + name
                    + "\" but "
                    + artifactId
                    + " not found on classpath. Make sure to add it as a dependency.");
          }
          throw new ConfigurationException("Unrecognized value for otel.logs.exporter: " + name);
        }
        return spiExporter;
    }
  }

  // Visible for testing
  @Nullable
  static LogRecordExporter configureOtlpLogs(ConfigProperties config, MeterProvider meterProvider) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_LOGS, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      try {
        ClasspathUtil.checkClassExists(
            "io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter",
            "OTLP HTTP Log Exporter",
            "opentelemetry-exporter-otlp-http-logs");
      } catch (ConfigurationException e) {
        // Squash this for now until logs are stable
        return null;
      }
      OtlpHttpLogRecordExporterBuilder builder = OtlpHttpLogRecordExporter.builder();

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
            "io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter",
            "OTLP gRPC Log Exporter",
            "opentelemetry-exporter-otlp-logs");
      } catch (ConfigurationException e) {
        // Squash this for now until logs are stable
        return null;
      }
      OtlpGrpcLogRecordExporterBuilder builder = OtlpGrpcLogRecordExporter.builder();

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

  private LogRecordExporterConfiguration() {}
}
