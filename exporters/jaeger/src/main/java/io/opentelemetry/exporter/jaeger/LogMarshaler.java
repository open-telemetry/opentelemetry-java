/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.Log;
import io.opentelemetry.sdk.trace.data.EventData;
import java.io.IOException;
import java.util.List;

final class LogMarshaler extends MarshalerWithSize {

  private static final AttributeKey<String> KEY_LOG_EVENT = AttributeKey.stringKey("event");
  private static final AttributeKey<Long> KEY_EVENT_DROPPED_ATTRIBUTES_COUNT =
      AttributeKey.longKey("otel.event.dropped_attributes_count");

  private final TimeMarshaler timestamp;
  private final List<KeyValueMarshaler> fields;

  static LogMarshaler[] createRepeated(List<EventData> events) {
    int len = events.size();
    LogMarshaler[] marshalers = new LogMarshaler[len];
    for (int i = 0; i < len; i++) {
      marshalers[i] = create(events.get(i));
    }
    return marshalers;
  }

  static LogMarshaler create(EventData event) {
    TimeMarshaler timestamp = TimeMarshaler.create(event.getEpochNanos());

    List<KeyValueMarshaler> fields = KeyValueMarshaler.createRepeated(event.getAttributes());

    // name is a top-level property in OpenTelemetry
    fields.add(KeyValueMarshaler.create(KEY_LOG_EVENT, event.getName()));

    int droppedAttributesCount = event.getDroppedAttributesCount();
    if (droppedAttributesCount > 0) {
      fields.add(
          KeyValueMarshaler.create(
              KEY_EVENT_DROPPED_ATTRIBUTES_COUNT, (long) droppedAttributesCount));
    }

    return new LogMarshaler(timestamp, fields);
  }

  LogMarshaler(TimeMarshaler timestamp, List<KeyValueMarshaler> fields) {
    super(calculateSize(timestamp, fields));
    this.timestamp = timestamp;
    this.fields = fields;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeMessage(Log.TIMESTAMP, timestamp);
    output.serializeRepeatedMessage(Log.FIELDS, fields);
  }

  private static int calculateSize(TimeMarshaler timestamp, List<KeyValueMarshaler> fields) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(Log.TIMESTAMP, timestamp);
    size += MarshalerUtil.sizeRepeatedMessage(Log.FIELDS, fields);
    return size;
  }
}
