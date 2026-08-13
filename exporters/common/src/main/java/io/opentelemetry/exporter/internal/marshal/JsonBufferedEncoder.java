/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * A minimal JSON encoder that serializes directly to an {@link OutputStream} as UTF-8, buffering
 * writes and implementing only the subset of JSON generation {@link JsonSerializer} needs so OTLP
 * JSON serialization has no third party dependency. Method names mirror the Jackson {@code
 * JsonGenerator} that previously backed {@link JsonSerializer}.
 *
 * <p>The caller is responsible for structural validity (matching braces); this class only inserts
 * separators between members. {@link #writeRaw(String)} writes verbatim without touching separator
 * state, which {@link MarshalerUtil#preserializeJsonFields(Marshaler)} relies on.
 *
 * <p>Nesting depth is capped at {@value #MAX_NESTING_DEPTH}. Attempting to nest deeper throws
 * {@link IOException} rather than {@link StackOverflowError}, which matters for recursive OTLP
 * structures such as cyclic {@code Value} bodies that could otherwise DoS an exporter. The cap is
 * intentionally conservative (Jackson 3's default is 500). Real OTLP payloads nest only a handful
 * of levels, and it is easier to relax the cap later than to tighten it.
 */
final class JsonBufferedEncoder {

  static final int MAX_NESTING_DEPTH = 100;

  private static final byte[] TRUE = {'t', 'r', 'u', 'e'};
  private static final byte[] FALSE = {'f', 'a', 'l', 's', 'e'};
  private static final byte[] HEX = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private final OutputStream out;
  private final byte[] buffer = new byte[4096];
  private int pos;

  // Whether any member has been written at each nesting depth; depth 0 is the implicit root. The
  // array grows on demand in push(), so it does not cap nesting depth.
  private boolean[] hasMembers = new boolean[16];
  private int depth;
  // True immediately after a field name has been written, meaning the next value is that field's
  // value and must not be preceded by a comma.
  private boolean afterKey;

  JsonBufferedEncoder(OutputStream out) {
    this.out = out;
  }

  void writeStartObject() throws IOException {
    beforeValue();
    writeByte((byte) '{');
    push();
  }

  void writeEndObject() throws IOException {
    pop();
    writeByte((byte) '}');
  }

  void writeObjectFieldStart(String name) throws IOException {
    writeFieldName(name);
    writeStartObject();
  }

  void writeArrayFieldStart(String name) throws IOException {
    writeFieldName(name);
    beforeValue();
    writeByte((byte) '[');
    push();
  }

  void writeEndArray() throws IOException {
    pop();
    writeByte((byte) ']');
  }

  void writeFieldName(String name) throws IOException {
    if (hasMembers[depth]) {
      writeByte((byte) ',');
    }
    hasMembers[depth] = true;
    writeQuoted(name);
    writeByte((byte) ':');
    afterKey = true;
  }

  void writeStringField(String name, String value) throws IOException {
    writeFieldName(name);
    writeString(value);
  }

  void writeBooleanField(String name, boolean value) throws IOException {
    writeFieldName(name);
    beforeValue();
    writeRawBytes(value ? TRUE : FALSE);
  }

  void writeNumberField(String name, int value) throws IOException {
    writeFieldName(name);
    beforeValue();
    writeAscii(Integer.toString(value));
  }

  void writeNumberField(String name, double value) throws IOException {
    writeFieldName(name);
    writeNumber(value);
  }

  void writeBinaryField(String name, byte[] value) throws IOException {
    writeFieldName(name);
    beforeValue();
    writeByte((byte) '"');
    writeRawBytes(Base64.getEncoder().encode(value));
    writeByte((byte) '"');
  }

  void writeString(String value) throws IOException {
    beforeValue();
    writeQuoted(value);
  }

  /**
   * Writes a JSON string value from already UTF-8 encoded bytes, avoiding a decode-then-re-encode
   * round trip. ASCII bytes are escaped as needed; multi-byte UTF-8 sequences pass through
   * verbatim.
   */
  void writeUtf8String(byte[] utf8Bytes) throws IOException {
    beforeValue();
    writeByte((byte) '"');
    for (byte b : utf8Bytes) {
      if (b >= 0) {
        writeEscapedAscii(b); // ASCII, may need escaping
      } else {
        writeByte(b); // multi-byte UTF-8, never escapable
      }
    }
    writeByte((byte) '"');
  }

  void writeNumber(double value) throws IOException {
    beforeValue();
    // proto3 JSON encodes the non-finite values as quoted strings; a bare NaN/Infinity is not valid
    // JSON. This matches io.opentelemetry.api.common.JsonEncoding.
    if (Double.isNaN(value)) {
      writeAscii("\"NaN\"");
    } else if (Double.isInfinite(value)) {
      writeAscii(value > 0 ? "\"Infinity\"" : "\"-Infinity\"");
    } else {
      writeAscii(Double.toString(value));
    }
  }

  /** Writes pre-serialized JSON verbatim, without updating separator state. */
  void writeRaw(String raw) throws IOException {
    writeRawBytes(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Drains buffered bytes to the underlying stream. Like {@code ProtoSerializer}, it neither
   * flushes nor closes the underlying stream; the caller owns its lifecycle.
   */
  void close() throws IOException {
    try {
      if (pos > 0) {
        out.write(buffer, 0, pos);
        pos = 0;
      }
    } catch (IOException e) {
      // In try-with-resources, draining may rethrow the same exception that failed the body; wrap
      // it so re-throwing doesn't trigger an IllegalArgumentException from illegal
      // self-suppression.
      throw new IOException(e);
    }
  }

  private void beforeValue() throws IOException {
    if (afterKey) {
      afterKey = false;
      return;
    }
    if (hasMembers[depth]) {
      writeByte((byte) ',');
    }
    hasMembers[depth] = true;
  }

  private void push() throws IOException {
    if (depth == MAX_NESTING_DEPTH) {
      throw new IOException(
          "JSON nesting depth exceeds maximum allowed (" + MAX_NESTING_DEPTH + ")");
    }
    depth++;
    if (depth == hasMembers.length) {
      hasMembers = Arrays.copyOf(hasMembers, hasMembers.length * 2);
    }
    hasMembers[depth] = false;
  }

  private void pop() {
    depth--;
  }

  /** Writes a quoted, escaped JSON string, encoding {@code value} as UTF-8. */
  private void writeQuoted(String value) throws IOException {
    writeByte((byte) '"');
    int length = value.length();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      if (c < 0x80) {
        writeEscapedAscii((byte) c);
      } else if (c < 0x800) {
        writeByte((byte) (0xC0 | (c >> 6)));
        writeByte((byte) (0x80 | (c & 0x3F)));
      } else if (Character.isHighSurrogate(c) && i + 1 < length) {
        char low = value.charAt(i + 1);
        if (Character.isLowSurrogate(low)) {
          int codePoint = Character.toCodePoint(c, low);
          writeByte((byte) (0xF0 | (codePoint >> 18)));
          writeByte((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
          writeByte((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
          writeByte((byte) (0x80 | (codePoint & 0x3F)));
          i++;
        } else {
          writeByte((byte) '?');
        }
      } else if (Character.isSurrogate(c)) {
        // Unpaired surrogate; emit a replacement to keep the output valid UTF-8.
        writeByte((byte) '?');
      } else {
        writeByte((byte) (0xE0 | (c >> 12)));
        writeByte((byte) (0x80 | ((c >> 6) & 0x3F)));
        writeByte((byte) (0x80 | (c & 0x3F)));
      }
    }
    writeByte((byte) '"');
  }

  /** Writes a single ASCII byte (0x00-0x7F), escaping it if required by JSON. */
  private void writeEscapedAscii(byte b) throws IOException {
    switch (b) {
      case '"':
        writeByte((byte) '\\');
        writeByte((byte) '"');
        return;
      case '\\':
        writeByte((byte) '\\');
        writeByte((byte) '\\');
        return;
      case '\b':
        writeByte((byte) '\\');
        writeByte((byte) 'b');
        return;
      case '\f':
        writeByte((byte) '\\');
        writeByte((byte) 'f');
        return;
      case '\n':
        writeByte((byte) '\\');
        writeByte((byte) 'n');
        return;
      case '\r':
        writeByte((byte) '\\');
        writeByte((byte) 'r');
        return;
      case '\t':
        writeByte((byte) '\\');
        writeByte((byte) 't');
        return;
      default:
        if (b < 0x20) {
          writeByte((byte) '\\');
          writeByte((byte) 'u');
          writeByte((byte) '0');
          writeByte((byte) '0');
          writeByte(HEX[(b >> 4) & 0xF]);
          writeByte(HEX[b & 0xF]);
        } else {
          writeByte(b);
        }
    }
  }

  /** Writes the bytes of an ASCII-only string such as a formatted number. */
  private void writeAscii(String value) throws IOException {
    int length = value.length();
    for (int i = 0; i < length; i++) {
      writeByte((byte) value.charAt(i));
    }
  }

  private void writeRawBytes(byte[] bytes) throws IOException {
    int offset = 0;
    int remaining = bytes.length;
    while (remaining > 0) {
      if (pos == buffer.length) {
        out.write(buffer, 0, pos);
        pos = 0;
      }
      int chunk = Math.min(remaining, buffer.length - pos);
      System.arraycopy(bytes, offset, buffer, pos, chunk);
      pos += chunk;
      offset += chunk;
      remaining -= chunk;
    }
  }

  private void writeByte(byte b) throws IOException {
    if (pos == buffer.length) {
      out.write(buffer, 0, pos);
      pos = 0;
    }
    buffer[pos++] = b;
  }
}
