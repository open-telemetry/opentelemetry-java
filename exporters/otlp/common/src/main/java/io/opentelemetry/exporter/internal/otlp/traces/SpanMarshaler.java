/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.api.trace.propagation.internal.W3CTraceContextEncoding.encodeTraceState;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

final class SpanMarshaler extends MarshalerWithSize {
  private static final byte[] EMPTY_BYTES = new byte[0];
  private final String traceId;
  private final byte[] traceStateUtf8;
  private final String spanId;
  @Nullable private final String parentSpanId;
  private final byte[] nameUtf8;
  private final ProtoEnumInfo spanKind;
  private final long startEpochNanos;
  private final long endEpochNanos;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final SpanEventMarshaler[] spanEventMarshalers;
  private final int droppedEventsCount;
  private final SpanLinkMarshaler[] spanLinkMarshalers;
  private final int droppedLinksCount;
  private final SpanStatusMarshaler spanStatusMarshaler;
  private final TraceFlags flags;

  // Because SpanMarshaler is always part of a repeated field, it cannot return "null".
  static SpanMarshaler create(SpanData spanData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(spanData.getAttributes());
    SpanEventMarshaler[] spanEventMarshalers =
        SpanEventMarshaler.createRepeated(spanData.getEvents());
    SpanLinkMarshaler[] spanLinkMarshalers = SpanLinkMarshaler.createRepeated(spanData.getLinks());

    String parentSpanId =
        spanData.getParentSpanContext().isValid()
            ? spanData.getParentSpanContext().getSpanId()
            : null;

    TraceState traceState = spanData.getSpanContext().getTraceState();
    byte[] traceStateUtf8 =
        traceState.isEmpty()
            ? EMPTY_BYTES
            : encodeTraceState(traceState).getBytes(StandardCharsets.UTF_8);

    return new SpanMarshaler(
        spanData.getSpanContext().getTraceId(),
        spanData.getSpanContext().getSpanId(),
        traceStateUtf8,
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
        SpanStatusMarshaler.create(spanData.getStatus()),
        spanData.getSpanContext().getTraceFlags());
  }

  private SpanMarshaler(
      String traceId,
      String spanId,
      byte[] traceStateUtf8,
      @Nullable String parentSpanId,
      byte[] nameUtf8,
      ProtoEnumInfo spanKind,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      SpanEventMarshaler[] spanEventMarshalers,
      int droppedEventsCount,
      SpanLinkMarshaler[] spanLinkMarshalers,
      int droppedLinksCount,
      SpanStatusMarshaler spanStatusMarshaler,
      TraceFlags flags) {
    super(
        calculateSize(
            traceId,
            spanId,
            traceStateUtf8,
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
            spanStatusMarshaler,
            flags));
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceStateUtf8 = traceStateUtf8;
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
    this.flags = flags;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeTraceId(Span.TRACE_ID, traceId);
    output.serializeSpanId(Span.SPAN_ID, spanId);
    output.serializeString(Span.TRACE_STATE, traceStateUtf8);
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
    output.serializeByteAsFixed32(Span.FLAGS, flags.asByte());
  }

  public static void writeTo(Serializer output, SpanData spanData, MarshalerContext context)
      throws IOException {
    output.serializeTraceId(Span.TRACE_ID, spanData.getTraceId(), context);
    output.serializeSpanId(Span.SPAN_ID, spanData.getSpanId(), context);

    byte[] traceStateUtf8 = context.getByteArray();
    output.serializeString(Span.TRACE_STATE, traceStateUtf8);
    String parentSpanId =
        spanData.getParentSpanContext().isValid()
            ? spanData.getParentSpanContext().getSpanId()
            : null;
    output.serializeSpanId(Span.PARENT_SPAN_ID, parentSpanId, context);

    if (context.marshalStringNoAllocation()) {
      output.writeString(Span.NAME, spanData.getName(), context.getSize());
    } else {
      byte[] nameUtf8 = context.getByteArray();
      output.serializeString(Span.NAME, nameUtf8);
    }

    output.serializeEnum(Span.KIND, toProtoSpanKind(spanData.getKind()));

    output.serializeFixed64(Span.START_TIME_UNIX_NANO, spanData.getStartEpochNanos());
    output.serializeFixed64(Span.END_TIME_UNIX_NANO, spanData.getEndEpochNanos());

    KeyValueMarshaler.writeTo(output, context, Span.ATTRIBUTES, spanData.getAttributes());
    int droppedAttributesCount =
        spanData.getTotalAttributeCount() - spanData.getAttributes().size();
    output.serializeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    output.serializeRepeatedMessage(
        Span.EVENTS, spanData.getEvents(), context, SpanEventMarshaler::writeTo);
    int droppedEventsCount = spanData.getTotalRecordedEvents() - spanData.getEvents().size();
    output.serializeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    output.serializeRepeatedMessage(
        Span.LINKS, spanData.getLinks(), context, SpanLinkMarshaler::writeTo);
    int droppedLinksCount = spanData.getTotalRecordedLinks() - spanData.getLinks().size();
    output.serializeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    output.serializeMessage(
        Span.STATUS, spanData.getStatus(), context, SpanStatusMarshaler::writeTo);

    output.serializeByteAsFixed32(Span.FLAGS, spanData.getSpanContext().getTraceFlags().asByte());
  }

  private static int calculateSize(
      String traceId,
      String spanId,
      byte[] traceStateUtf8,
      @Nullable String parentSpanId,
      byte[] nameUtf8,
      ProtoEnumInfo spanKind,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      SpanEventMarshaler[] spanEventMarshalers,
      int droppedEventsCount,
      SpanLinkMarshaler[] spanLinkMarshalers,
      int droppedLinksCount,
      SpanStatusMarshaler spanStatusMarshaler,
      TraceFlags flags) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(Span.SPAN_ID, spanId);
    size += MarshalerUtil.sizeBytes(Span.TRACE_STATE, traceStateUtf8);
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
    size += MarshalerUtil.sizeByteAsFixed32(Span.FLAGS, flags.asByte());

    return size;
  }

  public static int calculateSpanSize(MarshalerContext context, SpanData spanData) {
    int sizeIndex = context.addSize();
    int size = 0;
    size += MarshalerUtil.sizeTraceId(Span.TRACE_ID, spanData.getTraceId());
    size += MarshalerUtil.sizeSpanId(Span.SPAN_ID, spanData.getSpanId());

    TraceState traceState = spanData.getSpanContext().getTraceState();
    byte[] traceStateUtf8 =
        traceState.isEmpty()
            ? EMPTY_BYTES
            : encodeTraceState(traceState).getBytes(StandardCharsets.UTF_8);
    context.addData(traceStateUtf8);

    size += MarshalerUtil.sizeBytes(Span.TRACE_STATE, traceStateUtf8);
    String parentSpanId =
        spanData.getParentSpanContext().isValid()
            ? spanData.getParentSpanContext().getSpanId()
            : null;
    size += MarshalerUtil.sizeSpanId(Span.PARENT_SPAN_ID, parentSpanId);

    if (context.marshalStringNoAllocation()) {
      int utf8Size = MarshalerUtil.getUtf8Size(spanData.getName());
      context.addSize(utf8Size);
      size += MarshalerUtil.sizeBytes(Span.NAME, utf8Size);
    } else {
      byte[] nameUtf8 = MarshalerUtil.toBytes(spanData.getName());
      context.addData(nameUtf8);
      size += MarshalerUtil.sizeBytes(Span.NAME, nameUtf8);
    }

    size += MarshalerUtil.sizeEnum(Span.KIND, toProtoSpanKind(spanData.getKind()));

    size += MarshalerUtil.sizeFixed64(Span.START_TIME_UNIX_NANO, spanData.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(Span.END_TIME_UNIX_NANO, spanData.getEndEpochNanos());

    size += KeyValueMarshaler.calculateSize(Span.ATTRIBUTES, context, spanData.getAttributes());
    int droppedAttributesCount =
        spanData.getTotalAttributeCount() - spanData.getAttributes().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Span.EVENTS, SpanEventMarshaler::calculateSize, spanData.getEvents(), context);
    int droppedEventsCount = spanData.getTotalRecordedEvents() - spanData.getEvents().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_EVENTS_COUNT, droppedEventsCount);

    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Span.LINKS, SpanLinkMarshaler::calculateSize, spanData.getLinks(), context);
    int droppedLinksCount = spanData.getTotalRecordedLinks() - spanData.getLinks().size();
    size += MarshalerUtil.sizeUInt32(Span.DROPPED_LINKS_COUNT, droppedLinksCount);

    int spanStatusSize = SpanStatusMarshaler.calculateSize(context, spanData.getStatus());
    size += MarshalerUtil.sizeMessage(Span.STATUS, spanStatusSize);

    size +=
        MarshalerUtil.sizeByteAsFixed32(
            Span.FLAGS, spanData.getSpanContext().getTraceFlags().asByte());

    context.setSize(sizeIndex, size);

    return size;
  }

  // Visible for testing
  static ProtoEnumInfo toProtoSpanKind(SpanKind kind) {
    switch (kind) {
      case INTERNAL:
        return Span.SpanKind.SPAN_KIND_INTERNAL;
      case SERVER:
        return Span.SpanKind.SPAN_KIND_SERVER;
      case CLIENT:
        return Span.SpanKind.SPAN_KIND_CLIENT;
      case PRODUCER:
        return Span.SpanKind.SPAN_KIND_PRODUCER;
      case CONSUMER:
        return Span.SpanKind.SPAN_KIND_CONSUMER;
    }
    // NB: Should not be possible with aligned versions.
    return Span.SpanKind.SPAN_KIND_UNSPECIFIED;
  }
}
