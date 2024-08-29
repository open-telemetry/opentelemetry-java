/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;

/**
 * Exporter for sending OTLP log records to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutLogRecordExporter implements LogRecordExporter {
  private final OtlpJsonLoggingLogRecordExporter delegate;

  OtlpStdoutLogRecordExporter(OtlpJsonLoggingLogRecordExporter delegate) {
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpJsonLoggingLogRecordExporter} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingLogRecordExporter}.
   */
  public static LogRecordExporter create() {
    return builder().build();
  }

  /**
   * Returns a new {@link OtlpStdoutLogRecordExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpStdoutLogRecordExporterBuilder}.
   */
  @SuppressWarnings("SystemOut")
  public static OtlpStdoutLogRecordExporterBuilder builder() {
    return OtlpStdoutLogRecordExporterBuilder.create()
        .setOutputStream(System.out)
        .setWrapperJsonObject(true);
  }

  /**
   * Returns a new {@link OtlpStdoutLogRecordExporterBuilder} from an existing exporter.
   *
   * @return a new {@link OtlpStdoutLogRecordExporterBuilder}.
   */
  public OtlpStdoutLogRecordExporterBuilder toBuilder() {
    return new OtlpStdoutLogRecordExporterBuilder(
        OtlpJsonLoggingLogRecordExporterBuilder.createFromExporter(delegate));
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    return delegate.export(logs);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    return "OtlpStdoutLogRecordExporter{" + "delegate=" + delegate + '}';
  }
}
