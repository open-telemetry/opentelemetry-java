/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import javax.annotation.Nullable;

final class SpanMarshaler extends MarshalerWithSize {
  private final String traceId;
  private final String spanId;
  @Nullable private final String parentSpanId;
  private final byte[] nameUtf8;
  private final int spanKind;
  private final long startEpochNanos;
  private final long endEpochNanos;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final SpanEventMarshaler[] spanEventMarshalers;
  private final int droppedEventsCount;
  private final SpanLinkMarshaler[] spanLinkMarshalers;
  private final int droppedLinksCount;
  private final SpanStatusMarshaler spanStatusMarshaler;

  // Because SpanMarshaler is always part of a repeated field, it cannot return "null".
  static SpanMarshaler create(SpanData spanData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(spanData.getAttributes());
    SpanEventMarshaler[] spanEventMarshalers = SpanEventMarshaler.create(spanData.getEvents());
    SpanLinkMarshaler[] spanLinkMarshalers = SpanLinkMarshaler.create(spanData.getLinks());

    String parentSpanId =
        spanData.getParentSpanContext().isValid()
            ? spanData.getParentSpanContext().getSpanId()
            : null;

    return new SpanMarshaler(
        spanData.getSpanContext().getTraceId(),
        spanData.getSpanContext().getSpanId(),
        parentSpanId,
        MarshalerUtil.toBytes(spanData.getName()),
        toProtoSpanKind(spanData.getKind()),
        spanData.getStartEpochNanos(),
        spanData.getEndEpochNanos(),
        attributeMarshalers,
        spanData.getTotalAttributeCount() - spanData.getAttributes().size(),
        spanEventMarshalers,
        spanData.getTotalRecordedEvents() - spanData.getEvents().size(),
        spanLinkMarshalers,
        spanData.getTotalRecordedLinks() - spanData.getLinks().size(),
        SpanStatusMarshaler.create(spanData.getStatus()));
  }

  private SpanMarshaler(
      String traceId,
      String spanId,
      @Nullable String parentSpanId,
      byte[] nameUtf8,
      int spanKind,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      SpanEventMarshaler[] spanEventMarshalers,
      int droppedEventsCount,
      SpanLinkMarshaler[] spanLinkMarshalers,
      int droppedLinksCount,
      SpanStatusMarshaler spanStatusMarshaler) {
    super(
        calculateSize(
            traceId,
            spanId,
            parentSpanId,
            nameUtf8,
            spanKind,
            startEpochNanos,
            endEpochNanos,
            attributeMarshalers,
            droppedAttributesCount,
            spanEventMarshalers,
            droppedEventsCount,
            spanLinkMarshalers,
            droppedLinksCount,
            spanStatusMarshaler));
    this.traceId = traceId;
    this.spanId = spanId;
    this.parentSpanId = parentSpanId;
    this.nameUtf8 = nameUtf8;
    this.spanKind = spanKind;
    this.startEpochNanos = startEpochNanos;
    this.endEpochNanos = endEpochNanos;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
    this.spanEventMarshalers = spanEventMarshalers;
    this.droppedEventsCount = droppedEventsCount;
    this.spanLinkMarshalers = spanLinkMarshalers;
    this.droppedLinksCount = droppedLinksCount;
    this.spanStatusMarshaler = spanStatusMarshaler;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeTraceId(Span.TRACE_ID, traceId);
    output.serializeSpanId(Span.SPAN_ID, spanId);
    // TODO: Set TraceState;
    output.serializeSpanId(Span.PARENT_SPAN_ID, parentSpanId);
    output.serializeString(Span.NAME, nameUtf8);

    output.serializeEnum(Span.KIND, spanKind);

    output.serializeFixed64(Span.START_TIME_UNIX_NANO, startEpochNanos);
    output.serializeFixed64(Span.END_TIME_UNIX_NANO, endEpochNanos);

    output.serializeRepeatedMessage(Span.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    output.serializeRepeatedMessage(Span.EVENTS, spanEventMarshalers);
    output.serializeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    output.serializeRepeatedMessage(Span.LINKS, spanLinkMarshalers);
    output.serializeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    output.serializeMessage(Span.STATUS, spanStatusMarshaler);
  }

  private static int calculateSize(
      String traceId,
      String spanId,
      @Nullable String parentSpanId,
      byte[] nameUtf8,
      int spanKind,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      SpanEventMarshaler[] spanEventMarshalers,
      int droppedEventsCount,
      SpanLinkMarshaler[] spanLinkMarshalers,
      int droppedLinksCount,
      SpanStatusMarshaler spanStatusMarshaler) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(Span.SPAN_ID, spanId);
    // TODO: Set TraceState;
    size += MarshalerUtil.sizeSpanId(Span.PARENT_SPAN_ID, parentSpanId);
    size += MarshalerUtil.sizeBytes(Span.NAME, nameUtf8);

    size += MarshalerUtil.sizeEnum(Span.KIND, spanKind);

    size += MarshalerUtil.sizeFixed64(Span.START_TIME_UNIX_NANO, startEpochNanos);
    size += MarshalerUtil.sizeFixed64(Span.END_TIME_UNIX_NANO, endEpochNanos);

    size += MarshalerUtil.sizeRepeatedMessage(Span.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size += MarshalerUtil.sizeRepeatedMessage(Span.EVENTS, spanEventMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    size += MarshalerUtil.sizeRepeatedMessage(Span.LINKS, spanLinkMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    size += MarshalerUtil.sizeMessage(Span.STATUS, spanStatusMarshaler);
    return size;
  }

  private static int toProtoSpanKind(SpanKind kind) {
    switch (kind) {
      case INTERNAL:
        return Span.SpanKind.SPAN_KIND_INTERNAL_VALUE;
      case SERVER:
        return Span.SpanKind.SPAN_KIND_SERVER_VALUE;
      case CLIENT:
        return Span.SpanKind.SPAN_KIND_CLIENT_VALUE;
      case PRODUCER:
        return Span.SpanKind.SPAN_KIND_PRODUCER_VALUE;
      case CONSUMER:
        return Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;
    }
    return -1;
  }
}
