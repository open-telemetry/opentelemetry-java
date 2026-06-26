/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.Closeable;
import java.io.IOException;

interface JsonWriterTarget extends Closeable {

  void writeStartObject() throws IOException;

  void writeEndObject() throws IOException;

  void writeStartArray() throws IOException;

  void writeEndArray() throws IOException;

  void writeFieldName(String name) throws IOException;

  void writeString(String value) throws IOException;

  void writeNumber(int value) throws IOException;

  void writeNumber(long value) throws IOException;

  void writeNumber(double value) throws IOException;

  void writeBoolean(boolean value) throws IOException;

  void writeBinary(byte[] data) throws IOException;

  void writeRaw(String text) throws IOException;

  void writeRaw(char c) throws IOException;

  default void writeStringField(String fieldName, String value) throws IOException {
    writeFieldName(fieldName);
    writeString(value);
  }

  default void writeNumberField(String fieldName, int value) throws IOException {
    writeFieldName(fieldName);
    writeNumber(value);
  }

  default void writeNumberField(String fieldName, long value) throws IOException {
    writeFieldName(fieldName);
    writeNumber(value);
  }

  default void writeNumberField(String fieldName, double value) throws IOException {
    writeFieldName(fieldName);
    writeNumber(value);
  }

  default void writeBooleanField(String fieldName, boolean value) throws IOException {
    writeFieldName(fieldName);
    writeBoolean(value);
  }

  default void writeBinaryField(String fieldName, byte[] data) throws IOException {
    writeFieldName(fieldName);
    writeBinary(data);
  }

  default void writeObjectFieldStart(String fieldName) throws IOException {
    writeFieldName(fieldName);
    writeStartObject();
  }

  default void writeArrayFieldStart(String fieldName) throws IOException {
    writeFieldName(fieldName);
    writeStartArray();
  }
}
