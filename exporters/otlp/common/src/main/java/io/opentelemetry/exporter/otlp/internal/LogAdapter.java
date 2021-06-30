package io.opentelemetry.exporter.otlp.internal;


import java.util.List;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;

/** Converter from SDK {@link LogRecord} to OTLP {@link ResourceLogs}. */
public final class LogAdapter {

  /** Converts the provided {@link LogRecord} to {} */
  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecords) {

  }

}
