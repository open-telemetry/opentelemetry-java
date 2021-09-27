/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.logs;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.exporter.otlp.internal.StringAnyValueMarshaler;
import io.opentelemetry.proto.logs.v1.internal.LogRecord;
import io.opentelemetry.proto.logs.v1.internal.SeverityNumber;
import io.opentelemetry.sdk.logging.data.LogRecord.Severity;
import java.io.IOException;

final class LogMarshaler extends MarshalerWithSize {
  private final long timeUnixNano;
  private final ProtoEnumInfo severityNumber;
  private final byte[] severityText;
  private final byte[] nameUtf8;
  private final MarshalerWithSize anyValueMarshaler;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final int flags;
  private final String traceId;
  private final String spanId;

  static LogMarshaler create(io.opentelemetry.sdk.logging.data.LogRecord logRecord) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(logRecord.getAttributes());

    // For now, map all the bodies to String AnyValue.
    StringAnyValueMarshaler anyValueMarshaler =
        new StringAnyValueMarshaler(MarshalerUtil.toBytes(logRecord.getBody().asString()));

    return new LogMarshaler(
        logRecord.getTimeUnixNano(),
        toProtoSeverityNumber(logRecord.getSeverity()),
        MarshalerUtil.toBytes(logRecord.getSeverityText()),
        MarshalerUtil.toBytes(logRecord.getName()),
        anyValueMarshaler,
        attributeMarshalers,
        // TODO (trask) implement droppedAttributesCount in LogRecord
        0,
        logRecord.getFlags(),
        logRecord.getTraceId(),
        logRecord.getSpanId());
  }

  private LogMarshaler(
      long timeUnixNano,
      ProtoEnumInfo severityNumber,
      byte[] severityText,
      byte[] nameUtf8,
      MarshalerWithSize anyValueMarshaler,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      int flags,
      String traceId,
      String spanId) {
    super(
        calculateSize(
            timeUnixNano,
            severityNumber,
            severityText,
            nameUtf8,
            anyValueMarshaler,
            attributeMarshalers,
            droppedAttributesCount,
            flags,
            traceId,
            spanId));
    this.timeUnixNano = timeUnixNano;
    this.traceId = traceId;
    this.spanId = spanId;
    this.flags = flags;
    this.severityNumber = severityNumber;
    this.severityText = severityText;
    this.nameUtf8 = nameUtf8;
    this.anyValueMarshaler = anyValueMarshaler;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(LogRecord.TIME_UNIX_NANO, timeUnixNano);

    output.serializeEnum(LogRecord.SEVERITY_NUMBER, severityNumber);

    output.serializeString(LogRecord.SEVERITY_TEXT, severityText);

    output.serializeString(LogRecord.NAME, nameUtf8);

    output.serializeMessage(LogRecord.BODY, anyValueMarshaler);

    output.serializeRepeatedMessage(LogRecord.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    output.serializeFixed32(LogRecord.FLAGS, flags);
    output.serializeTraceId(LogRecord.TRACE_ID, traceId);
    output.serializeSpanId(LogRecord.SPAN_ID, spanId);
  }

  private static int calculateSize(
      long timeUnixNano,
      ProtoEnumInfo severityNumber,
      byte[] severityText,
      byte[] nameUtf8,
      MarshalerWithSize anyValueMarshaler,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      int flags,
      String traceId,
      String spanId) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(LogRecord.TIME_UNIX_NANO, timeUnixNano);

    size += MarshalerUtil.sizeEnum(LogRecord.SEVERITY_NUMBER, severityNumber);

    size += MarshalerUtil.sizeBytes(LogRecord.SEVERITY_TEXT, severityText);

    size += MarshalerUtil.sizeBytes(LogRecord.NAME, nameUtf8);

    size += MarshalerUtil.sizeMessage(LogRecord.BODY, anyValueMarshaler);

    size += MarshalerUtil.sizeRepeatedMessage(LogRecord.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size += MarshalerUtil.sizeFixed32(LogRecord.FLAGS, flags);
    size += MarshalerUtil.sizeTraceId(LogRecord.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(LogRecord.SPAN_ID, spanId);
    return size;
  }

  // Visible for testing
  static ProtoEnumInfo toProtoSeverityNumber(Severity severity) {
    switch (severity) {
      case TRACE:
        return SeverityNumber.SEVERITY_NUMBER_TRACE;
      case TRACE2:
        return SeverityNumber.SEVERITY_NUMBER_TRACE2;
      case TRACE3:
        return SeverityNumber.SEVERITY_NUMBER_TRACE3;
      case TRACE4:
        return SeverityNumber.SEVERITY_NUMBER_TRACE4;
      case DEBUG:
        return SeverityNumber.SEVERITY_NUMBER_DEBUG;
      case DEBUG2:
        return SeverityNumber.SEVERITY_NUMBER_DEBUG2;
      case DEBUG3:
        return SeverityNumber.SEVERITY_NUMBER_DEBUG3;
      case DEBUG4:
        return SeverityNumber.SEVERITY_NUMBER_DEBUG4;
      case INFO:
        return SeverityNumber.SEVERITY_NUMBER_INFO;
      case INFO2:
        return SeverityNumber.SEVERITY_NUMBER_INFO2;
      case INFO3:
        return SeverityNumber.SEVERITY_NUMBER_INFO3;
      case INFO4:
        return SeverityNumber.SEVERITY_NUMBER_INFO4;
      case WARN:
        return SeverityNumber.SEVERITY_NUMBER_WARN;
      case WARN2:
        return SeverityNumber.SEVERITY_NUMBER_WARN2;
      case WARN3:
        return SeverityNumber.SEVERITY_NUMBER_WARN3;
      case WARN4:
        return SeverityNumber.SEVERITY_NUMBER_WARN4;
      case ERROR:
        return SeverityNumber.SEVERITY_NUMBER_ERROR;
      case ERROR2:
        return SeverityNumber.SEVERITY_NUMBER_ERROR2;
      case ERROR3:
        return SeverityNumber.SEVERITY_NUMBER_ERROR3;
      case ERROR4:
        return SeverityNumber.SEVERITY_NUMBER_ERROR4;
      case FATAL:
        return SeverityNumber.SEVERITY_NUMBER_FATAL;
      case FATAL2:
        return SeverityNumber.SEVERITY_NUMBER_FATAL2;
      case FATAL3:
        return SeverityNumber.SEVERITY_NUMBER_FATAL3;
      case FATAL4:
        return SeverityNumber.SEVERITY_NUMBER_FATAL4;
      case UNDEFINED_SEVERITY_NUMBER:
        return SeverityNumber.SEVERITY_NUMBER_UNSPECIFIED;
    }
    // NB: Should not be possible with aligned versions.
    return SeverityNumber.SEVERITY_NUMBER_UNSPECIFIED;
  }
}
