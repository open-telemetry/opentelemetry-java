/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.common.Attributes;
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
    return String.valueOf(o);
  }
}
