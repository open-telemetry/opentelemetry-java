/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link LogRecordExporter} implementation that outputs log records to standard out. The output
 * is not intended to be comprehensive, but just usable for debugging.
 *
 * <p>Note: this doesn't use a {@code java.util.logging Logger}, as that could result in
 * logging-loops if an OTel appender is configured for {@code java.util.logging}.
 *
 * @since 1.19.0
 */
@SuppressWarnings("SystemOut")
public class SystemOutLogRecordExporter implements LogRecordExporter {
  private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  /** Returns a new {@link SystemOutLogRecordExporter}. */
  public static SystemOutLogRecordExporter create() {
    return new SystemOutLogRecordExporter();
  }

  private SystemOutLogRecordExporter() {}

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    StringBuilder stringBuilder = new StringBuilder(60);

    for (LogRecordData log : logs) {
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
  static void formatLog(StringBuilder stringBuilder, LogRecordData log) {
    InstrumentationScopeInfo instrumentationScopeInfo = log.getInstrumentationScopeInfo();
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
        .append(" [scopeInfo: ")
        .append(instrumentationScopeInfo.getName())
        .append(":")
        .append(
            instrumentationScopeInfo.getVersion() == null
                ? ""
                : instrumentationScopeInfo.getVersion())
        .append("] ")
        .append(log.getAttributes());
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      System.out.println("Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return CompletableResultCode.ofSuccess();
  }
}
