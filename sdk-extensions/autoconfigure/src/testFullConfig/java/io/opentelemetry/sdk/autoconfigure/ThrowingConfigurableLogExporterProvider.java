/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogExporterProvider;
import io.opentelemetry.sdk.logs.export.LogExporter;

public class ThrowingConfigurableLogExporterProvider implements ConfigurableLogExporterProvider {
  @Override
  public LogExporter createExporter(ConfigProperties config) {
    throw new IllegalStateException("always throws");
  }

  @Override
  public String getName() {
    return "throwing";
  }
}
