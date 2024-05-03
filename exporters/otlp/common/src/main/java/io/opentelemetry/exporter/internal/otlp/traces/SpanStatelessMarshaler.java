/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.exporter.internal.otlp.traces.SpanMarshaler.encodeSpanTraceState;
import static io.opentelemetry.exporter.internal.otlp.traces.SpanMarshaler.toProtoSpanKind;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.AttributeKeyValueStatelessMarshaler;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;

/** See {@link SpanMarshaler}. */
final class SpanStatelessMarshaler implements StatelessMarshaler<SpanData> {
  static final SpanStatelessMarshaler INSTANCE = new SpanStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, SpanData span, MarshalerContext context)
      throws IOException {
    output.serializeTraceId(Span.TRACE_ID, span.getTraceId(), context);
    output.serializeSpanId(Span.SPAN_ID, span.getSpanId(), context);

    byte[] traceStateUtf8 = context.getData(byte[].class);
    output.serializeString(Span.TRACE_STATE, traceStateUtf8);
    String parentSpanId =
        span.getParentSpanContext().isValid() ? span.getParentSpanContext().getSpanId() : null;
    output.serializeSpanId(Span.PARENT_SPAN_ID, parentSpanId, context);

    output.serializeStringWithContext(Span.NAME, span.getName(), context);
    output.serializeEnum(Span.KIND, toProtoSpanKind(span.getKind()));

    output.serializeFixed64(Span.START_TIME_UNIX_NANO, span.getStartEpochNanos());
    output.serializeFixed64(Span.END_TIME_UNIX_NANO, span.getEndEpochNanos());

    output.serializeRepeatedMessageWithContext(
        Span.ATTRIBUTES,
        span.getAttributes(),
        AttributeKeyValueStatelessMarshaler.INSTANCE,
        context);
    int droppedAttributesCount = span.getTotalAttributeCount() - span.getAttributes().size();
    output.serializeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    output.serializeRepeatedMessageWithContext(
        Span.EVENTS, span.getEvents(), SpanEventStatelessMarshaler.INSTANCE, context);
    int droppedEventsCount = span.getTotalRecordedEvents() - span.getEvents().size();
    output.serializeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    output.serializeRepeatedMessageWithContext(
        Span.LINKS, span.getLinks(), SpanLinkStatelessMarshaler.INSTANCE, context);
    int droppedLinksCount = span.getTotalRecordedLinks() - span.getLinks().size();
    output.serializeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    output.serializeMessageWithContext(
        Span.STATUS, span.getStatus(), SpanStatusStatelessMarshaler.INSTANCE, context);

    output.serializeFixed32(
        Span.FLAGS,
        SpanFlags.withParentIsRemoteFlags(
            span.getSpanContext().getTraceFlags(), span.getParentSpanContext().isRemote()));
  }

  @Override
  public int getBinarySerializedSize(SpanData span, MarshalerContext context) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.TRACE_ID, span.getTraceId());
    size += MarshalerUtil.sizeSpanId(Span.SPAN_ID, span.getSpanId());

    byte[] traceStateUtf8 = encodeSpanTraceState(span);
    context.addData(traceStateUtf8);

    size += MarshalerUtil.sizeBytes(Span.TRACE_STATE, traceStateUtf8);
    String parentSpanId =
        span.getParentSpanContext().isValid() ? span.getParentSpanContext().getSpanId() : null;
    size += MarshalerUtil.sizeSpanId(Span.PARENT_SPAN_ID, parentSpanId);

    size += StatelessMarshalerUtil.sizeStringWithContext(Span.NAME, span.getName(), context);
    size += MarshalerUtil.sizeEnum(Span.KIND, toProtoSpanKind(span.getKind()));

    size += MarshalerUtil.sizeFixed64(Span.START_TIME_UNIX_NANO, span.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(Span.END_TIME_UNIX_NANO, span.getEndEpochNanos());

    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            Span.ATTRIBUTES,
            span.getAttributes(),
            AttributeKeyValueStatelessMarshaler.INSTANCE,
            context);
    int droppedAttributesCount = span.getTotalAttributeCount() - span.getAttributes().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            Span.EVENTS, span.getEvents(), SpanEventStatelessMarshaler.INSTANCE, context);
    int droppedEventsCount = span.getTotalRecordedEvents() - span.getEvents().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            Span.LINKS, span.getLinks(), SpanLinkStatelessMarshaler.INSTANCE, context);
    int droppedLinksCount = span.getTotalRecordedLinks() - span.getLinks().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    size +=
        StatelessMarshalerUtil.sizeMessageWithContext(
            Span.STATUS, span.getStatus(), SpanStatusStatelessMarshaler.INSTANCE, context);

    size +=
        MarshalerUtil.sizeFixed32(
            Span.FLAGS,
            SpanFlags.withParentIsRemoteFlags(
                span.getSpanContext().getTraceFlags(), span.getParentSpanContext().isRemote()));

    return size;
  }
}
