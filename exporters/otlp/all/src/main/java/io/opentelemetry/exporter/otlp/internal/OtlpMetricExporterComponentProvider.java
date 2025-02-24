/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpHttpMetricExporter} and {@link
 * OtlpGrpcMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpMetricExporterComponentProvider implements ComponentProvider<MetricExporter> {

  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "otlp";
  }

  @Override
  public MetricExporter create(DeclarativeConfigProperties config) {
    String protocol = OtlpConfigUtil.getStructuredConfigOtlpProtocol(config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      OtlpHttpMetricExporterBuilder builder = httpBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          builder::setRetryPolicy,
          builder::setMemoryMode);
      ExporterBuilderUtil.configureOtlpAggregationTemporality(
          config, builder::setAggregationTemporalitySelector);
      ExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(
          config, builder::setDefaultAggregationSelector);

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      OtlpGrpcMetricExporterBuilder builder = grpcBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          builder::setRetryPolicy,
          builder::setMemoryMode);
      ExporterBuilderUtil.configureOtlpAggregationTemporality(
          config, builder::setAggregationTemporalitySelector);
      ExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(
          config, builder::setDefaultAggregationSelector);

      return builder.build();
    }
    throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
  }

  // Visible for testing
  OtlpHttpMetricExporterBuilder httpBuilder() {
    return OtlpHttpMetricExporter.builder();
  }

  // Visible for testing
  OtlpGrpcMetricExporterBuilder grpcBuilder() {
    return OtlpGrpcMetricExporter.builder();
  }
}
