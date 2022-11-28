/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.internal;

import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * {@link LogRecordExporter} SPI implementation for {@link SystemOutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class LoggingLogRecordExporterProvider implements ConfigurableLogRecordExporterProvider {
  @Override
  public LogRecordExporter createExporter(ConfigProperties config) {
    return SystemOutLogRecordExporter.create();
  }

  @Override
  public String getName() {
    return "logging";
  }
}
