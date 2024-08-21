/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;

/**
 * Exporter for sending OTLP log records to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutLogRecordExporter {
  private OtlpStdoutLogRecordExporter() {}

  /**
   * Returns a new {@link OtlpJsonLoggingLogRecordExporter} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingLogRecordExporter}.
   */
  public static OtlpJsonLoggingLogRecordExporter create() {
    return builder().build();
  }

  /**
   * Returns a new {@link OtlpJsonLoggingLogRecordExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingLogRecordExporterBuilder}.
   */
  @SuppressWarnings("SystemOut")
  public static OtlpJsonLoggingLogRecordExporterBuilder builder() {
    return OtlpJsonLoggingLogRecordExporterBuilder.create()
        .setOutputStream(System.out)
        .setWrapperJsonObject(true);
  }
}
