/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class StreamingJsonWriter implements JsonWriterTarget {

  private static final int BUFFER_SIZE = 8192;

  private final OutputStream out;
  private final byte[] buf = new byte[BUFFER_SIZE];
  private int pos;

  private boolean[] needsComma = new boolean[32];
  private int depth;

  StreamingJsonWriter(OutputStream out) {
    this.out = out;
  }

  @Override
  public void writeStartObject() throws IOException {
    maybeWriteComma();
    writeByte('{');
    pushContext();
  }

  @Override
  public void writeEndObject() throws IOException {
    popContext();
    writeByte('}');
  }

  @Override
  public void writeStartArray() throws IOException {
    maybeWriteComma();
    writeByte('[');
    pushContext();
  }

  @Override
  public void writeEndArray() throws IOException {
    popContext();
    writeByte(']');
  }

  @Override
  public void writeFieldName(String name) throws IOException {
    maybeWriteComma();
    // Reset so the field's value does not get a spurious comma.
    // The next writeFieldName or writeEnd* will handle commas correctly.
    needsComma[depth] = false;
    writeByte('"');
    writeEscaped(name);
    writeByte('"');
    writeByte(':');
  }

  @Override
  public void writeString(String value) throws IOException {
    maybeWriteComma();
    writeByte('"');
    writeEscaped(value);
    writeByte('"');
  }

  @Override
  public void writeNumber(int value) throws IOException {
    maybeWriteComma();
    writeAsciiString(Integer.toString(value));
  }

  @Override
  public void writeNumber(long value) throws IOException {
    maybeWriteComma();
    writeAsciiString(Long.toString(value));
  }

  @Override
  public void writeNumber(double value) throws IOException {
    maybeWriteComma();
    writeAsciiString(Double.toString(value));
  }

  @Override
  public void writeBoolean(boolean value) throws IOException {
    maybeWriteComma();
    writeAsciiString(value ? "true" : "false");
  }

  @Override
  public void writeBinary(byte[] data) throws IOException {
    maybeWriteComma();
    writeByte('"');
    writeAsciiString(Base64.getEncoder().encodeToString(data));
    writeByte('"');
  }

  @Override
  public void writeRaw(String text) throws IOException {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    if (pos + bytes.length > BUFFER_SIZE) {
      flush();
    }
    if (bytes.length > BUFFER_SIZE) {
      out.write(bytes);
      return;
    }
    System.arraycopy(bytes, 0, buf, pos, bytes.length);
    pos += bytes.length;
  }

  @Override
  public void writeRaw(char c) throws IOException {
    writeByte((byte) c);
  }

  @Override
  @SuppressWarnings("CheckedExceptionNotThrown")
  public void close() throws IOException {
    try {
      flush();
    } catch (IOException e) {
      // Swallow flush failure during close to avoid self-suppression when the same
      // IOException is thrown from both writeTo() and close() in a try-with-resources.
    }
  }

  private void flush() throws IOException {
    if (pos > 0) {
      out.write(buf, 0, pos);
      pos = 0;
    }
    out.flush();
  }

  private void writeByte(int b) throws IOException {
    if (pos >= BUFFER_SIZE) {
      flush();
    }
    buf[pos++] = (byte) b;
  }

  private void writeAsciiString(String ascii) throws IOException {
    int len = ascii.length();
    if (pos + len > BUFFER_SIZE) {
      flush();
    }
    if (len > BUFFER_SIZE) {
      out.write(ascii.getBytes(StandardCharsets.US_ASCII));
      return;
    }
    for (int i = 0; i < len; i++) {
      buf[pos++] = (byte) ascii.charAt(i);
    }
  }

  private void writeEscaped(String value) throws IOException {
    int[] posRef = {pos};
    JsonEscaping.escapeStringTo(buf, posRef, out, value);
    pos = posRef[0];
  }

  private void maybeWriteComma() throws IOException {
    if (depth > 0) {
      if (needsComma[depth]) {
        writeByte(',');
      } else {
        needsComma[depth] = true;
      }
    }
  }

  private void pushContext() {
    depth++;
    if (depth >= needsComma.length) {
      boolean[] newArray = new boolean[needsComma.length * 2];
      System.arraycopy(needsComma, 0, newArray, 0, needsComma.length);
      needsComma = newArray;
    }
    needsComma[depth] = false;
  }

  private void popContext() {
    depth--;
  }
}
