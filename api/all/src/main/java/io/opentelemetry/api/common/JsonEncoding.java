/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

final class JsonEncoding {

  private static final char[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  @SuppressWarnings("unchecked")
  static void append(StringBuilder sb, Value<?> value) {
    switch (value.getType()) {
      case STRING:
        appendString(sb, (String) value.getValue());
        break;
      case LONG:
        sb.append(value.getValue());
        break;
      case DOUBLE:
        appendDouble(sb, (Double) value.getValue());
        break;
      case BOOLEAN:
        sb.append(value.getValue());
        break;
      case ARRAY:
        appendArray(sb, (List<Value<?>>) value.getValue());
        break;
      case KEY_VALUE_LIST:
        appendMap(sb, (List<KeyValue>) value.getValue());
        break;
      case BYTES:
        appendBytes(sb, (ByteBuffer) value.getValue());
        break;
      case EMPTY:
        sb.append("null");
        break;
    }
  }

  private static void appendString(StringBuilder sb, String value) {
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

  private static void appendDouble(StringBuilder sb, double value) {
    if (Double.isNaN(value)) {
      sb.append("\"NaN\"");
    } else if (Double.isInfinite(value)) {
      sb.append(value > 0 ? "\"Infinity\"" : "\"-Infinity\"");
    } else {
      sb.append(value);
    }
  }

  private static void appendBytes(StringBuilder sb, ByteBuffer value) {
    byte[] bytes = new byte[value.remaining()];
    value.duplicate().get(bytes);
    sb.append('"').append(Base64.getEncoder().encodeToString(bytes)).append('"');
  }

  private static void appendArray(StringBuilder sb, List<Value<?>> values) {
    sb.append('[');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      append(sb, values.get(i));
    }
    sb.append(']');
  }

  private static void appendMap(StringBuilder sb, List<KeyValue> values) {
    sb.append('{');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      KeyValue kv = values.get(i);
      appendString(sb, kv.getKey());
      sb.append(':');
      append(sb, kv.getValue());
    }
    sb.append('}');
  }

  private JsonEncoding() {}
}
