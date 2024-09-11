/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
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
 * Exporter for sending OTLP log records to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutLogRecordExporter implements LogRecordExporter {

  private static final Logger LOGGER =
      Logger.getLogger(OtlpStdoutLogRecordExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final Logger logger;
  private final JsonWriter jsonWriter;
  private final boolean wrapperJsonObject;

  OtlpStdoutLogRecordExporter(Logger logger, JsonWriter jsonWriter, boolean wrapperJsonObject) {
    this.logger = logger;
    this.jsonWriter = jsonWriter;
    this.wrapperJsonObject = wrapperJsonObject;
  }

  /** Returns a new {@link OtlpStdoutLogRecordExporterBuilder}. */
  @SuppressWarnings("SystemOut")
  public static OtlpStdoutLogRecordExporterBuilder builder() {
    return new OtlpStdoutLogRecordExporterBuilder(LOGGER).setOutput(System.out);
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
    } else {
       jsonWriter.close();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpStdoutLogRecordExporter{", "}");
    joiner.add("jsonWriter=" + jsonWriter);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    return joiner.toString();
  }
}
