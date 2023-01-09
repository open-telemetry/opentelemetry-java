/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.function.Supplier;

/**
 * {@link MetricExporter} SPI implementation for {@link OtlpJsonLoggingMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class LoggingMetricExporterProvider implements ConfigurableMetricExporterProvider {
  @Override
  public MetricExporter createExporter(
      ConfigProperties config, Supplier<MeterProvider> meterProviderSupplier) {
    return OtlpJsonLoggingMetricExporter.create();
  }

  @Override
  public String getName() {
    return "logging-otlp";
  }
}
