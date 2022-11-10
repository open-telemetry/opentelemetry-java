/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;
import java.util.List;
import java.util.function.Function;

/**
 * Converts an EventData instance to a String representation of that data, with attributes converted
 * to JSON.
 *
 * <p>See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/sdk_exporters/zipkin.md#events">the
 * zipkin exporter spec</a> for details.
 */
class EventDataToAnnotation implements Function<EventData, String> {

  @Override
  public String apply(EventData eventData) {
    String name = eventData.getName();
    String value = toJson(eventData.getAttributes());
    return "\"" + name + "\":" + value;
  }

  private String toJson(Attributes attributes) {
    StringBuilder sb = new StringBuilder("{");
    attributes.forEach(
        (key, o) -> {
          if (sb.length() > 1) {
            sb.append(",");
          }
          sb.append("\"").append(key.getKey()).append("\":");
          String value = toValue(o);
          sb.append(value);
        });
    sb.append("}");
    return sb.toString();
  }

  private String toValue(Object o) {
    StringBuilder sb = new StringBuilder();
    if (o instanceof String) {
      sb.append("\"").append(o).append("\"");
    } else if (o instanceof List) {
      sb.append("[");
      ((List<?>) o)
          .forEach(
              v -> {
                if (sb.length() > 1) {
                  sb.append(",");
                }
                sb.append(toValue(v));
              });
      sb.append("]");
    } else {
      sb.append(o);
    }
    return sb.toString();
  }
}
