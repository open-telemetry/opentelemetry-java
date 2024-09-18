/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * File configuration SPI implementation for {@link OtlpStdoutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutLogRecordExporterComponentProvider
    implements ComponentProvider<LogRecordExporter> {

  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "experimental-otlp/stdout";
  }

  @Override
  public LogRecordExporter create(StructuredConfigProperties config) {
    OtlpStdoutLogRecordExporterBuilder builder = OtlpStdoutLogRecordExporter.builder();
    return builder.build();
  }
}
