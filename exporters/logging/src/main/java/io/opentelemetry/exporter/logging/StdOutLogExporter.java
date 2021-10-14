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
    StringBuilder sb = new StringBuilder(60);

    for (LogData log : logs) {
      sb.setLength(0);
      InstrumentationLibraryInfo instrumentationLibraryInfo = log.getInstrumentationLibraryInfo();
      sb.append("'")
          .append(log.getBody())
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
      System.out.println(sb);
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
