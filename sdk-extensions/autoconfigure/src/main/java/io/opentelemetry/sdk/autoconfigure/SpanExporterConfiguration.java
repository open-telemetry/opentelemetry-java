/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static java.util.stream.Collectors.toMap;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

final class SpanExporterConfiguration {

  private static final String EXPORTER_NONE = "none";
  private static final Map<String, String> EXPORTER_ARTIFACT_ID_BY_NAME;

  static {
    EXPORTER_ARTIFACT_ID_BY_NAME = new HashMap<>();
    EXPORTER_ARTIFACT_ID_BY_NAME.put("jaeger", "opentelemetry-exporter-jaeger");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging", "opentelemetry-exporter-logging");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("logging-otlp", "opentelemetry-exporter-logging-otlp");
    EXPORTER_ARTIFACT_ID_BY_NAME.put("zipkin", "opentelemetry-exporter-zipkin");
  }

  // Visible for testing
  static Map<String, SpanExporter> configureSpanExporters(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer) {
    Set<String> exporterNames = DefaultConfigProperties.getSet(config, "otel.traces.exporter");
    if (exporterNames.contains(EXPORTER_NONE)) {
      if (exporterNames.size() > 1) {
        throw new ConfigurationException(
            "otel.traces.exporter contains " + EXPORTER_NONE + " along with other exporters");
      }
      SpanExporter noop = SpanExporter.composite();
      SpanExporter customized = spanExporterCustomizer.apply(noop, config);
      if (customized == noop) {
        return Collections.emptyMap();
      }
      return Collections.singletonMap(EXPORTER_NONE, customized);
    }

    if (exporterNames.isEmpty()) {
      exporterNames = Collections.singleton("otlp");
    }

    NamedSpiManager<SpanExporter> spiExportersManager =
        spanExporterSpiManager(config, serviceClassLoader);

    return exporterNames.stream()
        .collect(
            toMap(
                Function.identity(),
                exporterName ->
                    spanExporterCustomizer.apply(
                        configureExporter(exporterName, config, spiExportersManager, meterProvider),
                        config)));
  }

  // Visible for testing
  static NamedSpiManager<SpanExporter> spanExporterSpiManager(
      ConfigProperties config, ClassLoader serviceClassLoader) {
    return SpiUtil.loadConfigurable(
        ConfigurableSpanExporterProvider.class,
        ConfigurableSpanExporterProvider::getName,
        ConfigurableSpanExporterProvider::createExporter,
        config,
        serviceClassLoader);
  }

  // Visible for testing
  static SpanExporter configureExporter(
      String name,
      ConfigProperties config,
      NamedSpiManager<SpanExporter> spiExportersManager,
      MeterProvider meterProvider) {
    switch (name) {
      case "otlp":
        return configureOtlp(config, meterProvider);
      default:
        SpanExporter spiExporter = spiExportersManager.getByName(name);
        if (spiExporter == null) {
          String artifactId = EXPORTER_ARTIFACT_ID_BY_NAME.get(name);
          if (artifactId != null) {
            throw new ConfigurationException(
                "otel.traces.exporter set to \""
                    + name
                    + "\" but "
                    + artifactId
                    + " not found on classpath. Make sure to add it as a dependency.");
          }
          throw new ConfigurationException("Unrecognized value for otel.traces.exporter: " + name);
        }
        return spiExporter;
    }
  }

  // Visible for testing
  static SpanExporter configureOtlp(ConfigProperties config, MeterProvider meterProvider) {
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
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

      builder.setMeterProvider(meterProvider);

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
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));
      builder.setMeterProvider(meterProvider);

      return builder.build();
    } else {
      throw new ConfigurationException("Unsupported OTLP traces protocol: " + protocol);
    }
  }

  private SpanExporterConfiguration() {}
}
