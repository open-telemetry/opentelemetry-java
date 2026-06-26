/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class JsonSerializer extends Serializer {

  private final JsonWriterTarget target;

  JsonSerializer(OutputStream output) {
    this.target = new StreamingJsonWriter(output);
  }

  JsonSerializer(JsonWriterTarget target) {
    this.target = target;
  }

  @Override
  protected void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException {
    target.writeStringField(field.getJsonName(), traceId);
  }

  @Override
  protected void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException {
    target.writeStringField(field.getJsonName(), spanId);
  }

  @Override
  public void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    target.writeBooleanField(field.getJsonName(), value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) throws IOException {
    target.writeNumberField(field.getJsonName(), enumValue.getEnumNumber());
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    target.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeSInt32(ProtoFieldInfo field, int value) throws IOException {
    target.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeint32(ProtoFieldInfo field, int value) throws IOException {
    target.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeInt64(ProtoFieldInfo field, long value) throws IOException {
    target.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64(ProtoFieldInfo field, long value) throws IOException {
    target.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    target.writeString(Long.toString(value));
  }

  @Override
  protected void writeUInt64Value(long value) throws IOException {
    target.writeString(Long.toString(value));
  }

  @Override
  public void writeUInt64(ProtoFieldInfo field, long value) throws IOException {
    target.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed32(ProtoFieldInfo field, int value) throws IOException {
    target.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeDouble(ProtoFieldInfo field, double value) throws IOException {
    target.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    target.writeNumber(value);
  }

  @Override
  public void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    target.writeFieldName(field.getJsonName());
    // Marshalers encode Strings into UTF-8 bytes to optimize for binary serialization where
    // we avoid the encoding process happening twice (once for size computation, once for writing).
    // It's wasteful to take a String, convert it to bytes, and convert back to the same String
    // but we can see if this can be improved in the future.
    target.writeString(new String(utf8Bytes, StandardCharsets.UTF_8));
  }

  @Override
  public void writeString(
      ProtoFieldInfo field, String string, int utf8Length, MarshalerContext context)
      throws IOException {
    target.writeFieldName(field.getJsonName());
    target.writeString(string);
  }

  @Override
  public void writeRepeatedString(ProtoFieldInfo field, byte[][] utf8Bytes) throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
    for (byte[] value : utf8Bytes) {
      // Marshalers encode Strings into UTF-8 bytes to optimize for binary serialization where
      // we avoid the encoding process happening twice (once for size computation, once for
      // writing).
      // It's wasteful to take a String, convert it to bytes, and convert back to the same String
      // but we can see if this can be improved in the future.
      target.writeString(new String(value, StandardCharsets.UTF_8));
    }
    target.writeEndArray();
  }

  @Override
  public void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    target.writeBinaryField(field.getJsonName(), value);
  }

  @Override
  public void writeByteBuffer(ProtoFieldInfo field, ByteBuffer value) throws IOException {
    byte[] data = new byte[value.capacity()];
    ((ByteBuffer) value.duplicate().clear()).get(data);
    target.writeBinaryField(field.getJsonName(), data);
  }

  @Override
  protected void writeStartMessage(ProtoFieldInfo field, int protoMessageSize) throws IOException {
    target.writeObjectFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndMessage() throws IOException {
    target.writeEndObject();
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedPrimitive() throws IOException {
    target.writeEndArray();
  }

  @Override
  protected void writeStartRepeatedVarint(ProtoFieldInfo field, int payloadSize)
      throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedVarint() throws IOException {
    target.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    target.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    target.writeEndArray();
  }

  @Override
  public <T> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context)
      throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
    for (int i = 0; i < messages.size(); i++) {
      T message = messages.get(i);
      target.writeStartObject();
      marshaler.writeTo(this, message, context);
      target.writeEndObject();
    }
    target.writeEndArray();
  }

  @Override
  public void writeStartRepeated(ProtoFieldInfo field) throws IOException {
    target.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  public void writeEndRepeated() throws IOException {
    target.writeEndArray();
  }

  @Override
  public void writeStartRepeatedElement(ProtoFieldInfo field, int protoMessageSize)
      throws IOException {
    target.writeStartObject();
  }

  @Override
  public void writeEndRepeatedElement() throws IOException {
    target.writeEndObject();
  }

  // Not a field.
  void writeMessageValue(Marshaler message) throws IOException {
    target.writeStartObject();
    message.writeTo(this);
    target.writeEndObject();
  }

  @Override
  public void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException {
    target.writeRaw(jsonSerialized);
  }

  @Override
  public void close() throws IOException {
    target.close();
  }
}
