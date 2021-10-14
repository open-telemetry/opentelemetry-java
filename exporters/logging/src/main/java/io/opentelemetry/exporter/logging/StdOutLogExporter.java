/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collection;

/**
 * A {@link LogExporter} implementation that outputs log records to standard out. The output is not
 * intended to be comprehensive, but just usable for debugging.
 */
@SuppressWarnings("SystemOut")
public class StdOutLogExporter implements LogExporter {

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

  // VisibleForTesting
  static void formatLog(StringBuilder stringBuilder, LogData log) {
    InstrumentationLibraryInfo instrumentationLibraryInfo = log.getInstrumentationLibraryInfo();
    stringBuilder
        .append(log.getEpochNanos())
        .append(" ")
        .append(log.getSeverity())
        .append(" '")
        .append(log.getBody().asString())
        .append("' : ")
        .append(log.getTraceId())
        .append(" ")
        .append(log.getSpanId())
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
