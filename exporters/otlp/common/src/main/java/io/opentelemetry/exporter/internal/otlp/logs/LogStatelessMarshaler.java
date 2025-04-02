/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import static io.opentelemetry.exporter.internal.otlp.logs.LogMarshaler.toProtoSeverityNumber;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.AnyValueStatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.AttributeKeyValueStatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.IncubatingUtil;
import io.opentelemetry.proto.logs.v1.internal.LogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import java.io.IOException;

/** See {@link LogMarshaler}. */
final class LogStatelessMarshaler implements StatelessMarshaler<LogRecordData> {

  private static final String INVALID_TRACE_ID = TraceId.getInvalid();
  private static final String INVALID_SPAN_ID = SpanId.getInvalid();
  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.common.ExtendedAttributes");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  static final LogStatelessMarshaler INSTANCE = new LogStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, LogRecordData log, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(LogRecord.TIME_UNIX_NANO, log.getTimestampEpochNanos());
    output.serializeFixed64(
        LogRecord.OBSERVED_TIME_UNIX_NANO, log.getObservedTimestampEpochNanos());
    output.serializeEnum(LogRecord.SEVERITY_NUMBER, toProtoSeverityNumber(log.getSeverity()));
    output.serializeStringWithContext(LogRecord.SEVERITY_TEXT, log.getSeverityText(), context);
    if (log.getBodyValue() != null) {
      output.serializeMessageWithContext(
          LogRecord.BODY, log.getBodyValue(), AnyValueStatelessMarshaler.INSTANCE, context);
    }

    int droppedAttributesCount;
    if (INCUBATOR_AVAILABLE) {
      IncubatingUtil.serializeExtendedAttributes(output, log, context);
      droppedAttributesCount =
          log.getTotalAttributeCount() - IncubatingUtil.extendedAttributesSize(log);
    } else {
      output.serializeRepeatedMessageWithContext(
          LogRecord.ATTRIBUTES,
          log.getAttributes(),
          AttributeKeyValueStatelessMarshaler.INSTANCE,
          context);
      droppedAttributesCount = log.getTotalAttributeCount() - log.getAttributes().size();
    }
    output.serializeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    SpanContext spanContext = log.getSpanContext();
    output.serializeFixed32(LogRecord.FLAGS, spanContext.getTraceFlags().asByte());
    if (!spanContext.getTraceId().equals(INVALID_TRACE_ID)) {
      output.serializeTraceId(LogRecord.TRACE_ID, spanContext.getTraceId(), context);
    }
    if (!spanContext.getSpanId().equals(INVALID_SPAN_ID)) {
      output.serializeSpanId(LogRecord.SPAN_ID, spanContext.getSpanId(), context);
    }
    if (log instanceof ExtendedLogRecordData) {
      output.serializeStringWithContext(
          LogRecord.EVENT_NAME, ((ExtendedLogRecordData) log).getEventName(), context);
    }
  }

  @Override
  public int getBinarySerializedSize(LogRecordData log, MarshalerContext context) {
    int size = 0;

    size += MarshalerUtil.sizeFixed64(LogRecord.TIME_UNIX_NANO, log.getTimestampEpochNanos());
    size +=
        MarshalerUtil.sizeFixed64(
            LogRecord.OBSERVED_TIME_UNIX_NANO, log.getObservedTimestampEpochNanos());
    size +=
        MarshalerUtil.sizeEnum(LogRecord.SEVERITY_NUMBER, toProtoSeverityNumber(log.getSeverity()));
    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            LogRecord.SEVERITY_TEXT, log.getSeverityText(), context);
    if (log.getBodyValue() != null) {
      size +=
          StatelessMarshalerUtil.sizeMessageWithContext(
              LogRecord.BODY, log.getBodyValue(), AnyValueStatelessMarshaler.INSTANCE, context);
    }
    if (INCUBATOR_AVAILABLE) {
      size += IncubatingUtil.sizeExtendedAttributes(log, context);

      int droppedAttributesCount =
          log.getTotalAttributeCount() - IncubatingUtil.extendedAttributesSize(log);
      size += MarshalerUtil.sizeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    } else {
      size +=
          StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
              LogRecord.ATTRIBUTES,
              log.getAttributes(),
              AttributeKeyValueStatelessMarshaler.INSTANCE,
              context);

      int droppedAttributesCount = log.getTotalAttributeCount() - log.getAttributes().size();
      size += MarshalerUtil.sizeUInt32(LogRecord.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    }

    SpanContext spanContext = log.getSpanContext();
    size += MarshalerUtil.sizeFixed32(LogRecord.FLAGS, spanContext.getTraceFlags().asByte());
    if (!spanContext.getTraceId().equals(INVALID_TRACE_ID)) {
      size += MarshalerUtil.sizeTraceId(LogRecord.TRACE_ID, spanContext.getTraceId());
    }
    if (!spanContext.getSpanId().equals(INVALID_SPAN_ID)) {
      size += MarshalerUtil.sizeSpanId(LogRecord.SPAN_ID, spanContext.getSpanId());
    }

    if (log instanceof ExtendedLogRecordData) {
      size +=
          StatelessMarshalerUtil.sizeStringWithContext(
              LogRecord.EVENT_NAME, ((ExtendedLogRecordData) log).getEventName(), context);
    }

    return size;
  }
}
