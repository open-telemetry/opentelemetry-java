/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class ThrowingConfigurableMetricExporterProvider
    implements ConfigurableMetricExporterProvider {
  @Override
  public MetricExporter createExporter(ConfigProperties config) {
    throw new IllegalStateException("always throws");
  }

  @Override
  public String getName() {
    return "throwing";
  }
}
