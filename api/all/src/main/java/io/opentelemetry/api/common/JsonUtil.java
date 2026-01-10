/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

/** Package-private utility for JSON encoding. */
final class JsonUtil {

  private static final char[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  @SuppressWarnings("unchecked")
  static void appendJsonValue(StringBuilder sb, Value<?> value) {
    switch (value.getType()) {
      case STRING:
        appendJsonString(sb, (String) value.getValue());
        break;
      case LONG:
        sb.append(value.getValue());
        break;
      case DOUBLE:
        appendJsonDouble(sb, (Double) value.getValue());
        break;
      case BOOLEAN:
        sb.append(value.getValue());
        break;
      case ARRAY:
        appendJsonArray(sb, (List<Value<?>>) value.getValue());
        break;
      case KEY_VALUE_LIST:
        appendJsonKeyValueList(sb, (List<KeyValue>) value.getValue());
        break;
      case BYTES:
        appendJsonBytes(sb, (ByteBuffer) value.getValue());
        break;
      case EMPTY:
        sb.append("null");
        break;
    }
  }

  static void appendJsonString(StringBuilder sb, String value) {
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) {
            // Control characters must be escaped as \\uXXXX
            sb.append("\\u");
            sb.append(HEX_DIGITS[(c >> 12) & 0xF]);
            sb.append(HEX_DIGITS[(c >> 8) & 0xF]);
            sb.append(HEX_DIGITS[(c >> 4) & 0xF]);
            sb.append(HEX_DIGITS[c & 0xF]);
          } else {
            sb.append(c);
          }
      }
    }
    sb.append('"');
  }

  private static void appendJsonDouble(StringBuilder sb, double value) {
    if (Double.isNaN(value)) {
      // Encoding as string to match ProtoJSON: https://protobuf.dev/programming-guides/json/
      sb.append("\"NaN\"");
    } else if (Double.isInfinite(value)) {
      // Encoding as string to match ProtoJSON: https://protobuf.dev/programming-guides/json/
      sb.append(value > 0 ? "\"Infinity\"" : "\"-Infinity\"");
    } else {
      sb.append(value);
    }
  }

  private static void appendJsonBytes(StringBuilder sb, ByteBuffer value) {
    // Encoding as base64 to match ProtoJSON: https://protobuf.dev/programming-guides/json/
    byte[] bytes = new byte[value.remaining()];
    value.duplicate().get(bytes);
    sb.append('"').append(Base64.getEncoder().encodeToString(bytes)).append('"');
  }

  private static void appendJsonArray(StringBuilder sb, List<Value<?>> values) {
    sb.append('[');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      appendJsonValue(sb, values.get(i));
    }
    sb.append(']');
  }

  private static void appendJsonKeyValueList(StringBuilder sb, List<KeyValue> values) {
    sb.append('{');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      KeyValue kv = values.get(i);
      appendJsonString(sb, kv.getKey());
      sb.append(':');
      appendJsonValue(sb, kv.getValue());
    }
    sb.append('}');
  }

  private JsonUtil() {}
}
