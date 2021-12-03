/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * A {@link LogExporter} implementation that outputs log records to standard out. The output is not
 * intended to be comprehensive, but just usable for debugging.
 *
 * <p>Note: this doesn't use a {@code java.util.logging Logger}, as that could result in
 * logging-loops if an OTel appender is configured for {@code java.util.logging}.
 */
@SuppressWarnings("SystemOut")
public class SystemOutLogExporter implements LogExporter {
  private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
    StringBuilder stringBuilder = new StringBuilder(60);

    for (LogData log : logs) {
      stringBuilder.setLength(0);
      formatLog(stringBuilder, log);
      System.out.println(stringBuilder);
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  // VisibleForTesting
  static void formatLog(StringBuilder stringBuilder, LogData log) {
    InstrumentationLibraryInfo instrumentationLibraryInfo = log.getInstrumentationLibraryInfo();
    stringBuilder
        .append(
            ISO_FORMAT.format(
                Instant.ofEpochMilli(NANOSECONDS.toMillis(log.getEpochNanos()))
                    .atZone(ZoneOffset.UTC)))
        .append(" ")
        .append(log.getSeverity())
        .append(" '")
        .append(log.getBody().asString())
        .append("' : ")
        .append(log.getSpanContext().getTraceId())
        .append(" ")
        .append(log.getSpanContext().getSpanId())
        .append(" [libraryInfo: ")
        .append(instrumentationLibraryInfo.getName())
        .append(":")
        .append(
            instrumentationLibraryInfo.getVersion() == null
                ? ""
                : instrumentationLibraryInfo.getVersion())
        .append("] ")
        .append(log.getAttributes());
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
