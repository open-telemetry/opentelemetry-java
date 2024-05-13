/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.api.incubator.logs.AnyValue;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.AnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.logs.v1.internal.LogRecord;
import io.opentelemetry.proto.logs.v1.internal.SeverityNumber;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.internal.AnyValueBody;
import java.io.IOException;
import javax.annotation.Nullable;

final class LogMarshaler extends MarshalerWithSize {
  private static final String INVALID_TRACE_ID = TraceId.getInvalid();
  private static final String INVALID_SPAN_ID = SpanId.getInvalid();
  private static final MarshalerWithSize EMPTY_BODY_MARSHALER =
      AnyValueMarshaler.create(AnyValue.of(""));

  private final long timeUnixNano;
  private final long observedTimeUnixNano;
  private final ProtoEnumInfo severityNumber;
  private final byte[] severityText;
  private final MarshalerWithSize anyValueMarshaler;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final TraceFlags traceFlags;
  @Nullable private final String traceId;
  @Nullable private final String spanId;

  static LogMarshaler create(LogRecordData logRecordData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(logRecordData.getAttributes());

    MarshalerWithSize bodyMarshaler = body(logRecordData.getBody());

    SpanContext spanContext = logRecordData.getSpanContext();
    return new LogMarshaler(
        logRecordData.getTimestampEpochNanos(),
        logRecordData.getObservedTimestampEpochNanos(),
        toProtoSeverityNumber(logRecordData.getSeverity()),
        MarshalerUtil.toBytes(logRecordData.getSeverityText()),
        bodyMarshaler,
        attributeMarshalers,
        logRecordData.getTotalAttributeCount() - logRecordData.getAttributes().size(),
        spanContext.getTraceFlags(),
        spanContext.getTraceId().equals(INVALID_TRACE_ID) ? null : spanContext.getTraceId(),
        spanContext.getSpanId().equals(INVALID_SPAN_ID) ? null : spanContext.getSpanId());
  }

  private static MarshalerWithSize body(Body body) {
    if (body instanceof AnyValueBody) {
      return AnyValueMarshaler.create(((AnyValueBody) body).asAnyValue());
    }
    switch (body.getType()) {
      case STRING:
        return AnyValueMarshaler.create(AnyValue.of(body.asString()));
      case EMPTY:
        return EMPTY_BODY_MARSHALER;
    }
    throw new IllegalStateException("Unsupported Body type: " + body.getType());
  }

  private LogMarshaler(
      long timeUnixNano,
      long observedTimeUnixNano,
      ProtoEnumInfo severityNumber,
      byte[] severityText,
      MarshalerWithSize anyValueMarshaler,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      TraceFlags traceFlags,
      @Nullable String traceId,
      @Nullable String spanId) {
    super(
        calculateSize(
            timeUnixNano,
            observedTimeUnixNano,
            severityNumber,
            severityText,
            anyValueMarshaler,
            attributeMarshalers,
            droppedAttributesCount,
            traceFlags,
            traceId,
            spanId));
    this.timeUnixNano = timeUnixNano;
    this.observedTimeUnixNano = observedTimeUnixNano;
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceFlags = traceFlags;
    this.severityNumber = severityNumber;
    this.severityText = severityText;
    this.anyValueMarshaler = anyValueMarshaler;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(LogRecord.TIME_UNIX_NANO, timeUnixNano);

    output.serializeFixed64(LogRecord.OBSERVED_TIME_UNIX_NANO, observedTimeUnixNano);

    output.serializeEnum(LogRecord.SEVERITY_NUMBER, severityNumber);

    output.serializeString(LogRecord.SEVERITY_TEXT, severityText);

    output.serializeMessage(LogRecord.BODY, anyValueMarshaler);

    output.serializeRepeatedMessage(LogRecord.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    output.serializeByteAsFixed32(LogRecord.FLAGS, traceFlags.asByte());
    output.serializeTraceId(LogRecord.TRACE_ID, traceId);
    output.serializeSpanId(LogRecord.SPAN_ID, spanId);
  }

  private static int calculateSize(
      long timeUnixNano,
      long observedTimeUnixNano,
      ProtoEnumInfo severityNumber,
      byte[] severityText,
      MarshalerWithSize anyValueMarshaler,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      TraceFlags traceFlags,
      @Nullable String traceId,
      @Nullable String spanId) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(LogRecord.TIME_UNIX_NANO, timeUnixNano);

    size += MarshalerUtil.sizeFixed64(LogRecord.OBSERVED_TIME_UNIX_NANO, observedTimeUnixNano);

    size += MarshalerUtil.sizeEnum(LogRecord.SEVERITY_NUMBER, severityNumber);

    size += MarshalerUtil.sizeBytes(LogRecord.SEVERITY_TEXT, severityText);

    size += MarshalerUtil.sizeMessage(LogRecord.BODY, anyValueMarshaler);

    size += MarshalerUtil.sizeRepeatedMessage(LogRecord.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size += MarshalerUtil.sizeByteAsFixed32(LogRecord.FLAGS, traceFlags.asByte());
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
