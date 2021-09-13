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
import io.opentelemetry.sdk.trace.data.EventData;
import java.io.IOException;
import java.util.List;

final class SpanEventMarshaler extends MarshalerWithSize {
  private static final SpanEventMarshaler[] EMPTY = new SpanEventMarshaler[0];
  private final long epochNanos;
  private final byte[] name;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;

  static SpanEventMarshaler[] create(List<EventData> events) {
    if (events.isEmpty()) {
      return EMPTY;
    }

    SpanEventMarshaler[] result = new SpanEventMarshaler[events.size()];
    int pos = 0;
    for (EventData event : events) {
      result[pos++] =
          new SpanEventMarshaler(
              event.getEpochNanos(),
              MarshalerUtil.toBytes(event.getName()),
              KeyValueMarshaler.createRepeated(event.getAttributes()),
              event.getTotalAttributeCount() - event.getAttributes().size());
    }

    return result;
  }

  private SpanEventMarshaler(
      long epochNanos,
      byte[] name,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    super(calculateSize(epochNanos, name, attributeMarshalers, droppedAttributesCount));
    this.epochNanos = epochNanos;
    this.name = name;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(Span.Event.TIME_UNIX_NANO, epochNanos);
    output.serializeString(Span.Event.NAME, name);
    output.serializeRepeatedMessage(Span.Event.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
  }

  private static int calculateSize(
      long epochNanos,
      byte[] name,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(Span.Event.TIME_UNIX_NANO, epochNanos);
    size += MarshalerUtil.sizeBytes(Span.Event.NAME, name);
    size += MarshalerUtil.sizeRepeatedMessage(Span.Event.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    return size;
  }
}
