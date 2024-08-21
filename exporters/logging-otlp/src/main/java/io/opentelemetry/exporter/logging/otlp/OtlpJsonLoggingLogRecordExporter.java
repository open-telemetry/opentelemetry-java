/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LogReusableDataMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.logs.LogRecordBuilderAccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpJsonLoggingLogRecordExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
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

  private final Function<Collection<LogRecordData>, CompletableResultCode> marshaler;
  private final MemoryMode memoryMode;
  private final boolean wrapperJsonObject;

  static {
    LogRecordBuilderAccessUtil.setToExporter(
        builder ->
            new OtlpJsonLoggingLogRecordExporter(
                builder.getJsonWriter(), builder.getMemoryMode(), builder.isWrapperJsonObject()));
    LogRecordBuilderAccessUtil.setToBuilder(
        exporter ->
            InternalBuilder.forLogs()
                .setJsonWriter(exporter.jsonWriter)
                .setWrapperJsonObject(exporter.wrapperJsonObject)
                .setMemoryMode(exporter.memoryMode));
  }

  /** Returns a new {@link OtlpJsonLoggingLogRecordExporter}. */
  public static LogRecordExporter create() {
    return OtlpJsonLoggingLogRecordExporterBuilder.create().build();
  }

  OtlpJsonLoggingLogRecordExporter(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {
    this.memoryMode = memoryMode;
    this.wrapperJsonObject = wrapperJsonObject;
    this.jsonWriter = jsonWriter;

    marshaler = createMarshaler(jsonWriter, memoryMode, wrapperJsonObject);
  }

  private static Function<Collection<LogRecordData>, CompletableResultCode> createMarshaler(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {

    if (wrapperJsonObject) {
      LogReusableDataMarshaler reusableDataMarshaler =
          new LogReusableDataMarshaler(memoryMode) {
            @Override
            public CompletableResultCode doExport(Marshaler exportRequest, int numItems) {
              return jsonWriter.write(exportRequest);
            }
          };

      return reusableDataMarshaler::export;
    } else {
      return logs -> {
        // not support for low allocation marshaler

        for (ResourceLogsMarshaler resourceLogs : ResourceLogsMarshaler.create(logs)) {
          CompletableResultCode resultCode = jsonWriter.write(resourceLogs);
          if (!resultCode.isSuccess()) {
            // already logged
            return resultCode;
          }
        }
        return CompletableResultCode.ofSuccess();
      };
    }
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    return marshaler.apply(logs);
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
    joiner.add("memoryMode=" + memoryMode);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    joiner.add("jsonWriter=" + jsonWriter);
    return joiner.toString();
  }
}
