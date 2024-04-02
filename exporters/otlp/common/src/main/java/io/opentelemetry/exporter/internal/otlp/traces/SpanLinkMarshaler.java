/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.api.trace.propagation.internal.W3CTraceContextEncoding.encodeTraceState;

import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class SpanLinkMarshaler extends MarshalerWithSize {
  private static final SpanLinkMarshaler[] EMPTY = new SpanLinkMarshaler[0];
  private static final byte[] EMPTY_BYTES = new byte[0];
  private final String traceId;
  private final String spanId;
  private final byte[] traceStateUtf8;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final TraceFlags traceFlags;

  static SpanLinkMarshaler[] createRepeated(List<LinkData> links) {
    if (links.isEmpty()) {
      return EMPTY;
    }

    SpanLinkMarshaler[] result = new SpanLinkMarshaler[links.size()];
    int pos = 0;
    for (LinkData link : links) {
      result[pos++] = create(link);
    }

    return result;
  }

  // Visible for testing
  static SpanLinkMarshaler create(LinkData link) {
    TraceState traceState = link.getSpanContext().getTraceState();
    byte[] traceStateUtf8 =
        traceState.isEmpty()
            ? EMPTY_BYTES
            : encodeTraceState(traceState).getBytes(StandardCharsets.UTF_8);
    return new SpanLinkMarshaler(
        link.getSpanContext().getTraceId(),
        link.getSpanContext().getSpanId(),
        link.getSpanContext().getTraceFlags(),
        traceStateUtf8,
        KeyValueMarshaler.createForAttributes(link.getAttributes()),
        link.getTotalAttributeCount() - link.getAttributes().size());
  }

  private SpanLinkMarshaler(
      String traceId,
      String spanId,
      TraceFlags traceFlags,
      byte[] traceStateUtf8,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    super(
        calculateSize(
            traceId,
            spanId,
            traceFlags,
            traceStateUtf8,
            attributeMarshalers,
            droppedAttributesCount));
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceFlags = traceFlags;
    this.traceStateUtf8 = traceStateUtf8;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeTraceId(Span.Link.TRACE_ID, traceId);
    output.serializeSpanId(Span.Link.SPAN_ID, spanId);
    output.serializeString(Span.Link.TRACE_STATE, traceStateUtf8);
    output.serializeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeByteAsFixed32(Span.Link.FLAGS, traceFlags.asByte());
  }

  public static void writeTo(Serializer output, LinkData link, MarshalerContext context)
      throws IOException {
    output.serializeTraceId(Span.Link.TRACE_ID, link.getSpanContext().getTraceId(), context);
    output.serializeSpanId(Span.Link.SPAN_ID, link.getSpanContext().getSpanId(), context);
    output.serializeString(Span.Link.TRACE_STATE, context.getByteArray());
    KeyValueMarshaler.writeTo(output, context, Span.Link.ATTRIBUTES, link.getAttributes());
    int droppedAttributesCount = link.getTotalAttributeCount() - link.getAttributes().size();
    output.serializeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeByteAsFixed32(Span.Link.FLAGS, link.getSpanContext().getTraceFlags().asByte());
  }

  private static int calculateSize(
      String traceId,
      String spanId,
      TraceFlags flags,
      byte[] traceStateUtf8,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.Link.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(Span.Link.SPAN_ID, spanId);
    size += MarshalerUtil.sizeBytes(Span.Link.TRACE_STATE, traceStateUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size += MarshalerUtil.sizeByteAsFixed32(Span.Link.FLAGS, flags.asByte());
    return size;
  }

  public static int calculateSize(LinkData link, MarshalerContext context) {
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
    size += KeyValueMarshaler.calculateSize(Span.Link.ATTRIBUTES, link.getAttributes(), context);
    int droppedAttributesCount = link.getTotalAttributeCount() - link.getAttributes().size();
    size += MarshalerUtil.sizeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size +=
        MarshalerUtil.sizeByteAsFixed32(
            Span.Link.FLAGS, link.getSpanContext().getTraceFlags().asByte());

    return size;
  }
}
