/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;

/**
 * Exporter for sending OTLP log records to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutMetricExporter {
  private OtlpStdoutMetricExporter() {}

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporter} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingMetricExporter}.
   */
  public static OtlpJsonLoggingMetricExporter create() {
    return builder().build();
  }

  /**
   * Returns a new {@link OtlpJsonLoggingMetricExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingMetricExporterBuilder}.
   */
  @SuppressWarnings("SystemOut")
  public static OtlpJsonLoggingMetricExporterBuilder builder() {
    return OtlpJsonLoggingMetricExporterBuilder.create()
        .setOutputStream(System.out)
        .setWrapperJsonObject(true);
  }
}
