/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.stream.metrics.StdoutMetricExporter;
import io.opentelemetry.exporter.otlp.stream.metrics.StdoutMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * File configuration SPI implementation for {@link
 * StdoutMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class StdoutMetricExporterComponentProvider
    implements ComponentProvider<MetricExporter> {

  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "otlp-stdout";
  }

  @Override
  public MetricExporter create(StructuredConfigProperties config) {
    StdoutMetricExporterBuilder builder = StdoutMetricExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
