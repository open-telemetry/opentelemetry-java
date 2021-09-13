/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

final class JsonSerializer extends Serializer {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private final JsonGenerator generator;

  JsonSerializer(OutputStream output) throws IOException {
    generator = JSON_FACTORY.createGenerator(output);
  }

  @Override
  protected void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException {
    generator.writeStringField(field.getJsonName(), traceId);
  }

  @Override
  protected void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException {
    generator.writeStringField(field.getJsonName(), spanId);
  }

  @Override
  protected void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    generator.writeBooleanField(field.getJsonName(), value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, int enumNumber) throws IOException {
    generator.writeNumberField(field.getJsonName(), enumNumber);
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    generator.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeInt64(ProtoFieldInfo field, long value) throws IOException {
    generator.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64(ProtoFieldInfo field, long value) throws IOException {
    generator.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    generator.writeString(Long.toString(value));
  }

  @Override
  protected void writeDouble(ProtoFieldInfo field, double value) throws IOException {
    generator.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    generator.writeNumber(value);
  }

  @Override
  protected void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    generator.writeFieldName(field.getJsonName());
    generator.writeUTF8String(utf8Bytes, 0, utf8Bytes.length);
  }

  @Override
  protected void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    generator.writeBinaryField(field.getJsonName(), value);
  }

  @Override
  protected void writeStartMessage(ProtoFieldInfo field, int protoMessageSize) throws IOException {
    generator.writeObjectFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndMessage() throws IOException {
    generator.writeEndObject();
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException {
    generator.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedPrimitive() throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException {
    generator.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    generator.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException {
    generator.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    generator.writeEndArray();
  }

  // Not a field.
  void writeMessageValue(Marshaler message) throws IOException {
    generator.writeStartObject();
    message.writeTo(this);
    generator.writeEndObject();
  }

  @Override
  public void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException {
    generator.writeRaw(jsonSerialized);
  }

  @Override
  public void close() throws IOException {
    generator.close();
  }
}
