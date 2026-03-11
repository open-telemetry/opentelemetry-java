/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A {@link LogRecordExporter} which writes {@linkplain LogRecordData logs} to a {@link Logger} in
 * OTLP JSON format. Each log line will include a single {@code ResourceLogs}.
 *
 * @since 1.19.0
 */
public final class OtlpJsonLoggingLogRecordExporter implements LogRecordExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName());

  private final OtlpStdoutLogRecordExporter delegate;

  /** Returns a new {@link OtlpJsonLoggingLogRecordExporter}. */
  public static LogRecordExporter create() {
    OtlpStdoutLogRecordExporter delegate =
        new OtlpStdoutLogRecordExporterBuilder(logger).setWrapperJsonObject(false).build();
    return new OtlpJsonLoggingLogRecordExporter(delegate);
  }

  OtlpJsonLoggingLogRecordExporter(OtlpStdoutLogRecordExporter delegate) {
    this.delegate = delegate;
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
}
