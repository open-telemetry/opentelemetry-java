/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.google.protobuf.UnsafeByteOperations;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.SeverityNumber;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converter from SDK {@link LogRecord} to OTLP {@link ResourceLogs}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LogAdapter {

  /** Returns a new List of {@link LogRecord}. */
  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecordList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>>
        resourceAndLibraryMap = groupByResourceAndLibrary(logRecordList);
    List<ResourceLogs> resourceLogs = new ArrayList<>(resourceAndLibraryMap.size());
    resourceAndLibraryMap.forEach(
        (resource, libraryInfoListMap) -> {
          ResourceLogs.Builder resourceLogsBuilder =
              ResourceLogs.newBuilder().setResource(ResourceAdapter.toProtoResource(resource));
          if (resource.getSchemaUrl() != null) {
            resourceLogsBuilder.setSchemaUrl(resource.getSchemaUrl());
          }
          libraryInfoListMap.forEach(
              (instrumentationLibraryInfo, logRecords) ->
                  resourceLogsBuilder.addInstrumentationLibraryLogs(
                      buildInstrumentationLibraryLogs(instrumentationLibraryInfo, logRecords)));
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

  private static Map<
          Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>>
      groupByResourceAndLibrary(Collection<LogRecord> logRecordList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>>
        result = new HashMap<>();
    for (LogRecord logRecord : logRecordList) {
      Map<InstrumentationLibraryInfo, List<io.opentelemetry.proto.logs.v1.LogRecord>>
          libraryInfoListMap =
              result.computeIfAbsent(logRecord.getResource(), unused -> new HashMap<>());
      List<io.opentelemetry.proto.logs.v1.LogRecord> logList =
          libraryInfoListMap.computeIfAbsent(
              logRecord.getInstrumentationLibraryInfo(), unused -> new ArrayList<>());
      logList.add(toProtoLogRecord(logRecord));
    }
    return result;
  }

  /** Returns a new {@link io.opentelemetry.proto.logs.v1.LogRecord} from a {@link LogRecord}. */
  static io.opentelemetry.proto.logs.v1.LogRecord toProtoLogRecord(LogRecord logRecord) {
    io.opentelemetry.proto.logs.v1.LogRecord.Builder builder =
        io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
            .setName(logRecord.getName())
            .setBody(getLogRecordBodyAnyValue(logRecord))
            .setSeverityText(logRecord.getSeverityText())
            .setSeverityNumber(
                SeverityNumber.forNumber(logRecord.getSeverity().getSeverityNumber()))
            .setTimeUnixNano(logRecord.getTimeUnixNano())
            .setTraceId(
                UnsafeByteOperations.unsafeWrap(
                    OtelEncodingUtils.bytesFromBase16(logRecord.getTraceId(), TraceId.getLength())))
            .setSpanId(
                UnsafeByteOperations.unsafeWrap(
                    OtelEncodingUtils.bytesFromBase16(logRecord.getSpanId(), SpanId.getLength())));

    logRecord
        .getAttributes()
        .forEach((key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));

    return builder.build();
  }

  private static AnyValue getLogRecordBodyAnyValue(LogRecord logRecord) {
    // For now, map all the bodies to String AnyValue.
    return AnyValue.newBuilder().setStringValue(logRecord.getBody().asString()).build();
  }

  private LogAdapter() {}
}
