package io.opentelemetry.exporter.otlp.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;

public class LogAdapter {

  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecordList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(logRecordList);
    List<ResourceLogs> resourceLogs = new ArrayList<>(resourceAndLibraryMap.size());
    resourceAndLibraryMap.forEach(
        (resource, libraryInfoListMap) -> {
          ResourceLogs.Builder resourceLogsBuilder =
              ResourceLogs.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
          if (resource.getSchemaUrl() != null) {
            resourceLogsBuilder.setSchemaUrl(resource.getSchemaUrl());
          }
          libraryInfoListMap.forEach(
              (instrumentationLibraryInfo, logRecords) -> {
                resourceLogsBuilder.addInstrumentationLibraryLogs(
                    buildInstrumentationLibraryLogs(instrumentationLibraryInfo, logRecords));
              });
          resourceLogs.add(resourceLogsBuilder.build());
        });
    return resourceLogs;
  }

  private static InstrumentationLibraryLogs buildInstrumentationLibraryLogs(
      InstrumentationLibraryInfo library, List<io.opentelemetry.proto.logs.v1.LogRecord> logs) {
    InstrumentationLibraryLogs.Builder logsBuilder =
        InstrumentationLibraryLogs.newBuilder()
          .setInstrumentationLibrary(CommonAdapter.toProtoInstrumentationLibrary(library))
          .addAllLogs(logs);
    if (library.getSchemaUrl() != null) {
      logsBuilder.setSchemaUrl(library.getSchemaUrl());
    }
    return logsBuilder.build();
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>>
      groupByResourceAndLibrary(Collection<LogRecord> logRecordList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>> result =
        new HashMap<>();
    for (LogRecord logRecord : logRecordList) {

      Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>> libraryInfoListMap =
        result.computeIfAbsent(logRecord.getResource(), unused -> new HashMap<>());
      List<io.opentelemetry.proto.logs.v1.LogRecord> logList =
          libraryInfoListMap.computeIfAbsent(
              logRecord.getInstrumentationLibraryInfo(), unused -> new ArrayList<>());
      logList.add(toProtoLogRecord(logRecord));
    }
    return result;
  }

  static io.opentelemetry.proto.logs.v1.LogRecord toProtoLogRecord(LogRecord logRecord) {
    io.opentelemetry.proto.logs.v1.LogRecord.Builder builder =
        io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
          .setName(logRecord.getName());

    // TODO implement this, here get the needed data to create the proper LogRecord model
    return builder.build();
  }

}






