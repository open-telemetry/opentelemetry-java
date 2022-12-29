/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

public class ThrowingConfigurableLogRecordExporterProvider
    implements ConfigurableLogRecordExporterProvider {
  @Override
  public LogRecordExporter createExporter(ConfigProperties config) {
    throw new IllegalStateException("always throws");
  }

  @Override
  public String getName() {
    return "throwing";
  }
}
