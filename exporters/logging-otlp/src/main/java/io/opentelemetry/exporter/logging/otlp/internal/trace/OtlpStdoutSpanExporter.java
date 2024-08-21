/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.trace;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;

/**
 * Exporter for sending OTLP log records to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutSpanExporter {
  private OtlpStdoutSpanExporter() {}

  /**
   * Returns a new {@link OtlpJsonLoggingSpanExporter} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingSpanExporter}.
   */
  public static OtlpJsonLoggingSpanExporter create() {
    return builder().build();
  }

  /**
   * Returns a new {@link OtlpJsonLoggingSpanExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingSpanExporterBuilder}.
   */
  @SuppressWarnings("SystemOut")
  public static OtlpJsonLoggingSpanExporterBuilder builder() {
    return OtlpJsonLoggingSpanExporterBuilder.create()
        .setOutputStream(System.out)
        .setWrapperJsonObject(true);
  }
}
