/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * {@link MetricExporter} SPI implementation for {@link OtlpGrpcMetricExporter} and {@link
 * OtlpHttpMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpMetricExporterProvider implements ConfigurableMetricExporterProvider {
  @Override
  public MetricExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_METRICS, config);

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
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));
      OtlpConfigUtil.configureOtlpAggregationTemporality(
          config, builder::setAggregationTemporalitySelector);
      OtlpConfigUtil.configureOtlpHistogramDefaultAggregation(
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
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));
      OtlpConfigUtil.configureOtlpAggregationTemporality(
          config, builder::setAggregationTemporalitySelector);
      OtlpConfigUtil.configureOtlpHistogramDefaultAggregation(
          config, builder::setDefaultAggregationSelector);

      return builder.build();
    }
    throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
  }

  @Override
  public String getName() {
    return "otlp";
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
