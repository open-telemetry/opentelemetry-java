/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

/**
 * Declarative configuration SPI implementation for {@link LoggingMetricExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ConsoleMetricExporterComponentProvider implements ComponentProvider {

  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "console";
  }

  @Override
  public MetricExporter create(DeclarativeConfigProperties config) {
    return LoggingMetricExporter.create();
  }
}
