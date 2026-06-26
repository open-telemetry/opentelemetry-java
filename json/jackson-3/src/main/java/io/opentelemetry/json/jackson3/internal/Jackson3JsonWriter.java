/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.json.jackson3.internal;

import io.opentelemetry.sdk.common.export.JsonWriter;
import java.io.IOException;
import tools.jackson.core.JsonGenerator;

/**
 * {@link JsonWriter} implementation backed by Jackson 3's {@link JsonGenerator}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("CheckedExceptionNotThrown")
final class Jackson3JsonWriter implements JsonWriter {

  private final JsonGenerator generator;

  Jackson3JsonWriter(JsonGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void writeStartObject() throws IOException {
    generator.writeStartObject();
  }

  @Override
  public void writeEndObject() throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void writeStartArray() throws IOException {
    generator.writeStartArray();
  }

  @Override
  public void writeEndArray() throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void writeFieldName(String name) throws IOException {
    generator.writeName(name);
  }

  @Override
  public void writeString(String value) throws IOException {
    generator.writeString(value);
  }

  @Override
  public void writeStringField(String fieldName, String value) throws IOException {
    generator.writeStringProperty(fieldName, value);
  }

  @Override
  public void writeNumber(int value) throws IOException {
    generator.writeNumber(value);
  }

  @Override
  public void writeNumber(long value) throws IOException {
    generator.writeNumber(value);
  }

  @Override
  public void writeNumber(double value) throws IOException {
    generator.writeNumber(value);
  }

  @Override
  public void writeNumberField(String fieldName, int value) throws IOException {
    generator.writeNumberProperty(fieldName, value);
  }

  @Override
  public void writeNumberField(String fieldName, long value) throws IOException {
    generator.writeNumberProperty(fieldName, value);
  }

  @Override
  public void writeNumberField(String fieldName, double value) throws IOException {
    generator.writeNumberProperty(fieldName, value);
  }

  @Override
  public void writeBoolean(boolean value) throws IOException {
    generator.writeBoolean(value);
  }

  @Override
  public void writeBooleanField(String fieldName, boolean value) throws IOException {
    generator.writeBooleanProperty(fieldName, value);
  }

  @Override
  public void writeBinary(byte[] data) throws IOException {
    generator.writeBinary(data);
  }

  @Override
  public void writeBinaryField(String fieldName, byte[] data) throws IOException {
    generator.writeBinaryProperty(fieldName, data);
  }

  @Override
  public void writeObjectFieldStart(String fieldName) throws IOException {
    generator.writeObjectPropertyStart(fieldName);
  }

  @Override
  public void writeArrayFieldStart(String fieldName) throws IOException {
    generator.writeArrayPropertyStart(fieldName);
  }

  @Override
  public void writeRaw(String text) throws IOException {
    generator.writeRaw(text);
  }

  @Override
  public void writeRaw(char c) throws IOException {
    generator.writeRaw(c);
  }

  @Override
  public void flush() throws IOException {
    generator.flush();
  }

  @Override
  public void close() throws IOException {
    generator.close();
  }
}
