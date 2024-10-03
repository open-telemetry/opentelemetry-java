/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * {@link LogRecordExporter} SPI implementation for {@link OtlpStdoutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutLogRecordExporterProvider implements ConfigurableLogRecordExporterProvider {
  @Override
  public LogRecordExporter createExporter(ConfigProperties config) {
    OtlpStdoutLogRecordExporterBuilder builder = OtlpStdoutLogRecordExporter.builder();
    return builder.build();
  }

  @Override
  public String getName() {
    return "experimental-otlp/stdout";
  }
}
