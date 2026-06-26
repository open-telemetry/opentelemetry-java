/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.util.Base64;

final class StringJsonWriter implements JsonWriterTarget {

  private final StringBuilder sb;

  private boolean[] needsComma = new boolean[32];
  private int depth;

  StringJsonWriter(StringBuilder sb) {
    this.sb = sb;
  }

  @Override
  public void writeStartObject() {
    maybeWriteComma();
    sb.append('{');
    pushContext();
  }

  @Override
  public void writeEndObject() {
    popContext();
    sb.append('}');
  }

  @Override
  public void writeStartArray() {
    maybeWriteComma();
    sb.append('[');
    pushContext();
  }

  @Override
  public void writeEndArray() {
    popContext();
    sb.append(']');
  }

  @Override
  public void writeFieldName(String name) {
    maybeWriteComma();
    needsComma[depth] = false;
    sb.append('"');
    JsonEscaping.escapeStringTo(sb, name);
    sb.append('"');
    sb.append(':');
  }

  @Override
  public void writeString(String value) {
    maybeWriteComma();
    sb.append('"');
    JsonEscaping.escapeStringTo(sb, value);
    sb.append('"');
  }

  @Override
  public void writeNumber(int value) {
    maybeWriteComma();
    sb.append(value);
  }

  @Override
  public void writeNumber(long value) {
    maybeWriteComma();
    sb.append(value);
  }

  @Override
  public void writeNumber(double value) {
    maybeWriteComma();
    sb.append(Double.toString(value));
  }

  @Override
  public void writeBoolean(boolean value) {
    maybeWriteComma();
    sb.append(value);
  }

  @Override
  public void writeBinary(byte[] data) {
    maybeWriteComma();
    sb.append('"');
    sb.append(Base64.getEncoder().encodeToString(data));
    sb.append('"');
  }

  @Override
  public void writeRaw(String text) {
    sb.append(text);
  }

  @Override
  public void writeRaw(char c) {
    sb.append(c);
  }

  @Override
  public void close() {
    // no-op
  }

  private void maybeWriteComma() {
    if (depth > 0) {
      if (needsComma[depth]) {
        sb.append(',');
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
