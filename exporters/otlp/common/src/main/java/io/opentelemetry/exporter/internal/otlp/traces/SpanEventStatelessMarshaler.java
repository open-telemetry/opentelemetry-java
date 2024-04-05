/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.trace.v1.internal.Span;
import io.opentelemetry.sdk.trace.data.EventData;
import java.io.IOException;

final class SpanEventStatelessMarshaler implements StatelessMarshaler<EventData> {
  static final SpanEventStatelessMarshaler INSTANCE = new SpanEventStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, EventData event, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(Span.Event.TIME_UNIX_NANO, event.getEpochNanos());
    output.serializeString(Span.Event.NAME, event.getName(), context);
    output.serializeRepeatedMessage(
        Span.Event.ATTRIBUTES, event.getAttributes(), KeyValueStatelessMarshaler.INSTANCE, context);
    int droppedAttributesCount = event.getTotalAttributeCount() - event.getAttributes().size();
    output.serializeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
  }

  @Override
  public int getBinarySerializedSize(EventData event, MarshalerContext context) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(Span.Event.TIME_UNIX_NANO, event.getEpochNanos());
    size += MarshalerUtil.sizeString(Span.Event.NAME, event.getName(), context);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Span.Event.ATTRIBUTES,
            event.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);
    int droppedAttributesCount = event.getTotalAttributeCount() - event.getAttributes().size();
    size += MarshalerUtil.sizeUInt32(Span.Event.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);

    return size;
  }
}
