/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * {@link MetricExporter} SPI implementation for {@link OtlpStdoutMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutMetricExporterProvider implements ConfigurableMetricExporterProvider {
  @Override
  public MetricExporter createExporter(ConfigProperties config) {
    OtlpStdoutMetricExporterBuilder builder = OtlpStdoutMetricExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    ExporterBuilderUtil.configureOtlpAggregationTemporality(
        config, builder::setAggregationTemporalitySelector);
    ExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(
        config, builder::setDefaultAggregationSelector);
    return builder.build();
  }

  @Override
  public String getName() {
    return "experimental-otlp/stdout";
  }
}
