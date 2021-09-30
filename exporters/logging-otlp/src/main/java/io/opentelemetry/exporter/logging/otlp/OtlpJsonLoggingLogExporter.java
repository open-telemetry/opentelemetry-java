/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.exporter.logging.otlp.JsonUtil.JSON_FACTORY;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.exporter.otlp.internal.logs.ResourceLogsMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.LogExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link LogExporter} which writes {@linkplain LogRecord logs} to a {@link Logger} in OTLP JSON
 * format. Each log line will include a single {@code ResourceLogs}.
 */
public final class OtlpJsonLoggingLogExporter implements LogExporter {

  //  private static final MessageMarshaller marshaller =
  //      MessageMarshaller.builder()
  //          .register(ResourceLogs.class)
  //          .omittingInsignificantWhitespace(true)
  //          .build();

  private static final Logger logger = Logger.getLogger(OtlpJsonLoggingLogExporter.class.getName());

  /** Returns a new {@link OtlpJsonLoggingLogExporter}. */
  public static LogExporter create() {
    return new OtlpJsonLoggingLogExporter();
  }

  private OtlpJsonLoggingLogExporter() {}

  @Override
  public CompletableResultCode export(Collection<LogRecord> logs) {
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
    return CompletableResultCode.ofSuccess();
  }
}
