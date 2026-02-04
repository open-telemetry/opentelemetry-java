/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_METRICS;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.internal.IncubatingExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedComponentProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpHttpMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpHttpMetricExporterComponentProvider implements ExtendedComponentProvider {

  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "otlp_http";
  }

  @Override
  public MetricExporter create(DeclarativeConfigProperties config, ConfigProvider configProvider) {
    OtlpHttpMetricExporterBuilder builder = httpBuilder();

    OtlpDeclarativeConfigUtil.configureOtlpExporterBuilder(
        DATA_TYPE_METRICS,
        config,
        configProvider,
        builder::setComponentLoader,
        builder::setEndpoint,
        builder::addHeader,
        builder::setCompression,
        builder::setTimeout,
        builder::setTrustedCertificates,
        builder::setClientTls,
        builder::setRetryPolicy,
        builder::setMemoryMode,
        /* isHttpProtobuf= */ true,
        builder::setInternalTelemetryVersion);
    IncubatingExporterBuilderUtil.configureOtlpAggregationTemporality(
        config, builder::setAggregationTemporalitySelector);
    IncubatingExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(
        config, builder::setDefaultAggregationSelector);

    return builder.build();
  }

  // Visible for testing
  OtlpHttpMetricExporterBuilder httpBuilder() {
    return OtlpHttpMetricExporter.builder();
  }
}
