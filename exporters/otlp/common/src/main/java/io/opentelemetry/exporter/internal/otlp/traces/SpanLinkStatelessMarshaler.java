/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.api.trace.propagation.internal.W3CTraceContextEncoding.encodeTraceState;

import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class SpanLinkStatelessMarshaler implements StatelessMarshaler<LinkData> {
  static final SpanLinkStatelessMarshaler INSTANCE = new SpanLinkStatelessMarshaler();
  private static final byte[] EMPTY_BYTES = new byte[0];

  @Override
  public void writeTo(Serializer output, LinkData link, MarshalerContext context)
      throws IOException {
    output.serializeTraceId(Span.Link.TRACE_ID, link.getSpanContext().getTraceId(), context);
    output.serializeSpanId(Span.Link.SPAN_ID, link.getSpanContext().getSpanId(), context);
    output.serializeString(Span.Link.TRACE_STATE, context.getData(byte[].class));
    output.serializeRepeatedMessage(
        Span.Link.ATTRIBUTES, link.getAttributes(), KeyValueStatelessMarshaler.INSTANCE, context);
    int droppedAttributesCount = link.getTotalAttributeCount() - link.getAttributes().size();
    output.serializeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeFixed32(
        Span.Link.FLAGS,
        SpanFlags.withParentIsRemoteFlags(
            link.getSpanContext().getTraceFlags(), link.getSpanContext().isRemote()));
  }

  @Override
  public int getBinarySerializedSize(LinkData link, MarshalerContext context) {
    TraceState traceState = link.getSpanContext().getTraceState();
    byte[] traceStateUtf8 =
        traceState.isEmpty()
            ? EMPTY_BYTES
            : encodeTraceState(traceState).getBytes(StandardCharsets.UTF_8);
    context.addData(traceStateUtf8);

    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.Link.TRACE_ID, link.getSpanContext().getTraceId());
    size += MarshalerUtil.sizeSpanId(Span.Link.SPAN_ID, link.getSpanContext().getSpanId());
    size += MarshalerUtil.sizeBytes(Span.Link.TRACE_STATE, traceStateUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Span.Link.ATTRIBUTES,
            link.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);
    int droppedAttributesCount = link.getTotalAttributeCount() - link.getAttributes().size();
    size += MarshalerUtil.sizeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size +=
        MarshalerUtil.sizeFixed32(
            Span.Link.FLAGS,
            SpanFlags.withParentIsRemoteFlags(
                link.getSpanContext().getTraceFlags(), link.getSpanContext().isRemote()));

    return size;
  }
}
