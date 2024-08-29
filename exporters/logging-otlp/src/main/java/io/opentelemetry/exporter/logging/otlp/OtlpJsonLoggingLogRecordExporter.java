/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.logging.otlp.internal.logs.LogRecordBuilderAccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
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

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final JsonWriter jsonWriter;

  private final boolean wrapperJsonObject;

  static {
    LogRecordBuilderAccessUtil.setToExporter(
        argument ->
            new OtlpJsonLoggingLogRecordExporter(
                argument.getJsonWriter(), argument.isWrapperJsonObject()));
    LogRecordBuilderAccessUtil.setToBuilder(
        exporter ->
            new LogRecordBuilderAccessUtil.Argument(
                exporter.jsonWriter, exporter.wrapperJsonObject));
  }

  /** Returns a new {@link OtlpJsonLoggingLogRecordExporter}. */
  public static LogRecordExporter create() {
    return OtlpStdoutLogRecordExporterBuilder.create().build();
  }

  OtlpJsonLoggingLogRecordExporter(JsonWriter jsonWriter, boolean wrapperJsonObject) {
    this.wrapperJsonObject = wrapperJsonObject;
    this.jsonWriter = jsonWriter;
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    if (wrapperJsonObject) {
      LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);
      return jsonWriter.write(request);
    } else {
      for (ResourceLogsMarshaler resourceLogs : ResourceLogsMarshaler.create(logs)) {
        CompletableResultCode resultCode = jsonWriter.write(resourceLogs);
        if (!resultCode.isSuccess()) {
          // already logged
          return resultCode;
        }
      }
      return CompletableResultCode.ofSuccess();
    }
  }

  @Override
  public CompletableResultCode flush() {
    return jsonWriter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpJsonLoggingLogRecordExporter{", "}");
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    joiner.add("jsonWriter=" + jsonWriter);
    return joiner.toString();
  }
}
