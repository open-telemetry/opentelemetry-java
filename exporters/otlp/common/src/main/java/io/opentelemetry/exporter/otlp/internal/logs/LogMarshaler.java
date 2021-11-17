/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.logs;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.exporter.otlp.internal.StringAnyValueMarshaler;
import io.opentelemetry.proto.logs.v1.internal.LogRecord;
import io.opentelemetry.proto.logs.v1.internal.SeverityNumber;
import io.opentelemetry.sdk.logs.data.Severity;
import java.io.IOException;
import javax.annotation.Nullable;

final class LogMarshaler extends MarshalerWithSize {
  private static final String INVALID_TRACE_ID = TraceId.getInvalid();
  private static final String INVALID_SPAN_ID = SpanId.getInvalid();

  private final long timeUnixNano;
  private final ProtoEnumInfo severityNumber;
  private final byte[] severityText;
  private final byte[] nameUtf8;
  private final MarshalerWithSize anyValueMarshaler;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final TraceFlags traceFlags;
  @Nullable private final String traceId;
  @Nullable private final String spanId;

  static LogMarshaler create(io.opentelemetry.sdk.logs.data.LogData logData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(logData.getAttributes());

    // For now, map all the bodies to String AnyValue.
    StringAnyValueMarshaler anyValueMarshaler =
        new StringAnyValueMarshaler(MarshalerUtil.toBytes(logData.getBody().asString()));

    SpanContext spanContext = logData.getSpanContext();
    return new LogMarshaler(
        logData.getEpochNanos(),
        toProtoSeverityNumber(logData.getSeverity()),
        MarshalerUtil.toBytes(logData.getSeverityText()),
        MarshalerUtil.toBytes(logData.getName()),
        anyValueMarshaler,
        attributeMarshalers,
        // TODO (trask) implement droppedAttributesCount in LogRecord
        0,
        spanContext.getTraceFlags(),
        spanContext.getTraceId().equals(INVALID_TRACE_ID) ? null : spanContext.getTraceId(),
        spanContext.getSpanId().equals(INVALID_SPAN_ID) ? null : spanContext.getSpanId());
  }

  private LogMarshaler(
      long timeUnixNano,
      ProtoEnumInfo severityNumber,
      byte[] severityText,
      byte[] nameUtf8,
      MarshalerWithSize anyValueMarshaler,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      TraceFlags traceFlags,
      @Nullable String traceId,
      @Nullable String spanId) {
    super(
        calculateSize(
            timeUnixNano,
            severityNumber,
            severityText,
            nameUtf8,
            anyValueMarshaler,
            attributeMarshalers,
            droppedAttributesCount,
            traceFlags,
            traceId,
            spanId));
    this.timeUnixNano = timeUnixNano;
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceFlags = traceFlags;
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

    output.serializeFixed32(LogRecord.FLAGS, toUnsignedInt(traceFlags.asByte()));
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
      TraceFlags traceFlags,
      @Nullable String traceId,
      @Nullable String spanId) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(LogRecord.TIME_UNIX_NANO, timeUnixNano);

    size += MarshalerUtil.sizeEnum(LogRecord.SEVERITY_NUMBER, severityNumber);

    size += MarshalerUtil.sizeBytes(LogRecord.SEVERITY_TEXT, severityText);

    size += MarshalerUtil.sizeBytes(LogRecord.NAME, nameUtf8);

    size += MarshalerUtil.sizeMessage(LogRecord.BODY, anyValueMarshaler);

    size += MarshalerUtil.sizeRepeatedMessage(LogRecord.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size += MarshalerUtil.sizeFixed32(LogRecord.FLAGS, toUnsignedInt(traceFlags.asByte()));
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

  /** Vendored {@link Byte#toUnsignedInt(byte)} to support Android. */
  private static int toUnsignedInt(byte x) {
    return ((int) x) & 0xff;
  }
}
