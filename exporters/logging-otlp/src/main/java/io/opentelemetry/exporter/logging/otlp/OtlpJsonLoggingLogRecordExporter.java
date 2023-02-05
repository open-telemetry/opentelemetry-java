/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.exporter.logging.otlp.JsonUtil.JSON_FACTORY;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.IOException;
import java.util.Collection;
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

  /** Returns a new {@link OtlpJsonLoggingLogRecordExporter}. */
  public static LogRecordExporter create() {
    return new OtlpJsonLoggingLogRecordExporter();
  }

  private OtlpJsonLoggingLogRecordExporter() {}

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    ResourceLogsMarshaler[] allResourceLogs = ResourceLogsMarshaler.create(logs);
    for (ResourceLogsMarshaler resourceLogs : allResourceLogs) {
      SegmentedStringWriter sw = new SegmentedStringWriter(JSON_FACTORY._getBufferRecycler());
      try (JsonGenerator gen = JsonUtil.create(sw)) {
        resourceLogs.writeJsonTo(gen);
      } catch (IOException e) {
        // Shouldn't happen in practice, just skip it.
        continue;
      }
      logger.log(Level.INFO, sw.getAndClear());
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }
}
