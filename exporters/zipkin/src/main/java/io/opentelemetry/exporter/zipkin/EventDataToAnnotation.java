/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import io.opentelemetry.sdk.trace.data.EventData;
import java.util.List;

/**
 * Converts an EventData instance to a String representation of that data, with attributes converted
 * to JSON.
 *
 * <p>See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/sdk_exporters/zipkin.md#events">the
 * zipkin exporter spec</a> for details.
 */
final class EventDataToAnnotation {

  private EventDataToAnnotation() {}

  static String apply(EventData eventData) {
    String name = eventData.getName();
    String value = toJson(eventData.getAttributes());
    return "\"" + name + "\":" + value;
  }

  private static String toJson(Attributes attributes) {
    return attributes.asMap().entrySet().stream()
        .map(entry -> "\"" + entry.getKey() + "\":" + toValue(entry.getValue()))
        .collect(joining(",", "{", "}"));
  }

  private static String toValue(Object o) {
    if (o instanceof String) {
      return "\"" + o + "\"";
    }
    if (o instanceof List) {
      return ((List<?>) o)
          .stream().map(EventDataToAnnotation::toValue).collect(joining(",", "[", "]"));
    }
    if (o instanceof Value) {
      return toJsonValue((Value<?>) o);
    }
    return String.valueOf(o);
  }

  // note: simple types (STRING, BOOLEAN, LONG, DOUBLE) won't actually come here
  // but handling here for completeness
  private static String toJsonValue(Value<?> value) {
    ValueType type = value.getType();
    switch (type) {
      case STRING:
      case BYTES:
        // For JSON encoding, strings and bytes need to be quoted
        return "\"" + value.asString() + "\"";
      case EMPTY:
        // For JSON encoding, empty values should be null
        return "null";
      case ARRAY:
      case KEY_VALUE_LIST:
      case BOOLEAN:
      case LONG:
      case DOUBLE:
        // Arrays, maps, and primitives are already valid JSON from asString()
        return value.asString();
    }
    throw new IllegalStateException("Unknown value type: " + type);
  }
}
