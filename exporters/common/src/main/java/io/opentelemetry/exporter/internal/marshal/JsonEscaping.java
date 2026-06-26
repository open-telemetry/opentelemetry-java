/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class JsonEscaping {

  private static final char[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  static void escapeStringTo(StringBuilder sb, String value) {
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
  }

  static void escapeStringTo(byte[] buf, int[] posRef, OutputStream out, String value)
      throws IOException {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      String escape = null;
      switch (c) {
        case '"':
          escape = "\\\"";
          break;
        case '\\':
          escape = "\\\\";
          break;
        case '\b':
          escape = "\\b";
          break;
        case '\f':
          escape = "\\f";
          break;
        case '\n':
          escape = "\\n";
          break;
        case '\r':
          escape = "\\r";
          break;
        case '\t':
          escape = "\\t";
          break;
        default:
          if (c < 0x20) {
            escape =
                "\\u"
                    + HEX_DIGITS[(c >> 12) & 0xF]
                    + HEX_DIGITS[(c >> 8) & 0xF]
                    + HEX_DIGITS[(c >> 4) & 0xF]
                    + HEX_DIGITS[c & 0xF];
          }
      }
      if (escape != null) {
        writeAscii(buf, posRef, out, escape);
      } else {
        // Non-escape character: find the run of non-escape chars and write them as a UTF-8 chunk
        int start = i;
        while (i + 1 < value.length()) {
          char next = value.charAt(i + 1);
          if (next == '"' || next == '\\' || next < 0x20) {
            break;
          }
          i++;
        }
        writeUtf8(buf, posRef, out, value, start, i + 1);
      }
    }
  }

  private static void writeAscii(byte[] buf, int[] posRef, OutputStream out, String ascii)
      throws IOException {
    int pos = posRef[0];
    int len = ascii.length();
    if (pos + len > buf.length) {
      out.write(buf, 0, pos);
      pos = 0;
    }
    for (int i = 0; i < len; i++) {
      buf[pos++] = (byte) ascii.charAt(i);
    }
    posRef[0] = pos;
  }

  private static void writeUtf8(
      byte[] buf, int[] posRef, OutputStream out, String value, int start, int end)
      throws IOException {
    byte[] utf8 = value.substring(start, end).getBytes(StandardCharsets.UTF_8);
    int pos = posRef[0];
    if (pos + utf8.length > buf.length) {
      out.write(buf, 0, pos);
      pos = 0;
    }
    if (utf8.length > buf.length) {
      out.write(utf8);
      posRef[0] = pos;
      return;
    }
    System.arraycopy(utf8, 0, buf, pos, utf8.length);
    posRef[0] = pos + utf8.length;
  }

  private JsonEscaping() {}
}
