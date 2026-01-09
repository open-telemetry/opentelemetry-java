/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link MetricExporter} SPI implementation for {@link OtlpGrpcMetricExporter} and {@link
 * OtlpHttpMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpMetricExporterProvider
    implements ConfigurableMetricExporterProvider, AutoConfigureListener {

  private final AtomicReference<MeterProvider> meterProviderRef =
      new AtomicReference<>(MeterProvider.noop());

  @Override
  public MetricExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_METRICS, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      OtlpHttpMetricExporterBuilder builder = httpBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setComponentLoader,
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
      builder.setMeterProvider(meterProviderRef::get);

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      OtlpGrpcMetricExporterBuilder builder = grpcBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_METRICS,
          config,
          builder::setComponentLoader,
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
      builder.setMeterProvider(meterProviderRef::get);

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

  @Override
  public void afterAutoConfigure(OpenTelemetrySdk sdk) {
    meterProviderRef.set(sdk.getMeterProvider());
  }
}
