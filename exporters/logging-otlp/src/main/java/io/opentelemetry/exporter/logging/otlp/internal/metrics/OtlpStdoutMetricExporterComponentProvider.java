/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.internal.IncuatingExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpStdoutMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutMetricExporterComponentProvider
    implements ComponentProvider<MetricExporter> {

  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "experimental-otlp/stdout";
  }

  @Override
  public MetricExporter create(DeclarativeConfigProperties config) {
    OtlpStdoutMetricExporterBuilder builder = OtlpStdoutMetricExporter.builder();
    IncuatingExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    IncuatingExporterBuilderUtil.configureOtlpAggregationTemporality(
        config, builder::setAggregationTemporalitySelector);
    IncuatingExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(
        config, builder::setDefaultAggregationSelector);
    return builder.build();
  }
}
