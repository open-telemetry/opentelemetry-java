package io.opentelemetry.exporter.otlp.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import jdk.internal.net.http.common.Log;
import sun.net.www.http.HttpCaptureOutputStream;

public class LogAdapter {


  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecordList) {
    // Instrumentation
    Map<Resource, Map<InstrumentationLibraryInfo, List<>>> resourceAndLibraryMap =

        groupByResourceAndLibrary(logRecordList);
    List<ResourceLogs> resourceLogs = new ArrayList<>(resourceAndLibraryMap.size());

    resourceAndLibraryMap.forEach(
        (resource, libraryLogs) -> {
          ResourceLogs.Builder resourceLogsBuilder =
              ResourceLogs.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
        }
    );

  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>>
      groupByResourceAndLibrary(Collection<LogRecord> logRecordList) {

    Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> result = new HashMap<>();

    for (LogRecord logRecord : logRecordList) {

      Map<InstrumentationLibraryInfo, List<LogRecord>> libraryInfoListMap =
        result.computeIfAbsent(logRecord.getResource(), unused -> new HashMap<>());

      List<LogRecord> logList =
          libraryInfoListMap.computeIfAbsent(
              logRecord.getInstrumentationLibraryInfo(), unused -> new ArrayList<>());

      // TODO convert to log record
      logList.add(toProtoLog(logRecord));
    }

    return result;
  }

}






