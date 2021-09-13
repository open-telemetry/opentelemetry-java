/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.io.IOException;
import java.util.List;

final class SpanLinkMarshaler extends MarshalerWithSize {
  private static final SpanLinkMarshaler[] EMPTY = new SpanLinkMarshaler[0];
  private final String traceId;
  private final String spanId;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;

  static SpanLinkMarshaler[] create(List<LinkData> links) {
    if (links.isEmpty()) {
      return EMPTY;
    }

    SpanLinkMarshaler[] result = new SpanLinkMarshaler[links.size()];
    int pos = 0;
    for (LinkData link : links) {
      result[pos++] =
          new SpanLinkMarshaler(
              link.getSpanContext().getTraceId(),
              link.getSpanContext().getSpanId(),
              KeyValueMarshaler.createRepeated(link.getAttributes()),
              link.getTotalAttributeCount() - link.getAttributes().size());
    }

    return result;
  }

  private SpanLinkMarshaler(
      String traceId,
      String spanId,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    super(calculateSize(traceId, spanId, attributeMarshalers, droppedAttributesCount));
    this.traceId = traceId;
    this.spanId = spanId;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeTraceId(Span.Link.TRACE_ID, traceId);
    output.serializeSpanId(Span.Link.SPAN_ID, spanId);
    // TODO: Set TraceState;
    output.serializeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
  }

  private static int calculateSize(
      String traceId,
      String spanId,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.Link.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(Span.Link.SPAN_ID, spanId);
    // TODO: Set TraceState;
    size += MarshalerUtil.sizeRepeatedMessage(Span.Link.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.Link.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    return size;
  }
}
