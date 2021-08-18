/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogAdapter {

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(LogAdapter.class.getName()));

  private LogAdapter() {}

  /** Converts the provided {@link LogRecord} to {@link ResourceLogs}. */
  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecords) {
    List<ResourceLogs> resourceLogs = new ArrayList<>(logRecords.size());
    Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(logRecords);

    resourceAndLibraryMap.forEach(
        (resource, libraryLogs) -> {
          ResourceLogs.Builder resourceLogsBuilder =
              ResourceLogs.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
          if (resource.getSchemaUrl() != null) {
            resourceLogsBuilder.setSchemaUrl(resource.getSchemaUrl());
          }
          libraryLogs.forEach(
              (library, spans) -> {
                resourceLogsBuilder.addInstrumentationLibraryLogs(
                    buildInstrumentationLibraryLog(library, spans));
              });
          resourceLogs.add(resourceLogsBuilder.build());
        });

    return resourceLogs;
  }

  private static InstrumentationLibraryLogs buildInstrumentationLibraryLog(
      InstrumentationLibraryInfo library, List<LogRecord> logs) {
    InstrumentationLibraryLogs.Builder logsBuilder =
        InstrumentationLibraryLogs.newBuilder()
            .setInstrumentationLibrary(CommonAdapter.toProtoInstrumentationLibrary(library))
            .addAllLogs(logs);
    if (library.getSchemaUrl() != null) {
      logsBuilder.setSchemaUrl(library.getSchemaUrl());
    }
    return logsBuilder.build();
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> groupByResourceAndLibrary(
      Collection<LogRecord> logRecordsList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> result = new HashMap<>();
    for (LogRecord logRecord : logRecordsList) {
      Map<InstrumentationLibraryInfo, List<LogRecord>> libraryInfoListMap =
          result.computeIfAbsent(logRecord.getResource(), unused -> new HashMap<>());
      Collection<LogRecord> logRecordList =
          libraryInfoListMap.computeIfAbsent(
              logRecord.getInstrumentationLibraryInfo(), unused -> new ArrayList<>());
      logRecordList.add(toProtoResourceLogs(logRecord));
    }
    return result;
  }
}
