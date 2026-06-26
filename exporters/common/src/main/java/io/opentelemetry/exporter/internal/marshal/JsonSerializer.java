/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.sdk.common.export.JsonWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class JsonSerializer extends Serializer {

  private final JsonWriter writer;

  JsonSerializer(JsonWriter writer) {
    this.writer = writer;
  }

  @Override
  protected void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException {
    writer.writeStringField(field.getJsonName(), traceId);
  }

  @Override
  protected void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException {
    writer.writeStringField(field.getJsonName(), spanId);
  }

  @Override
  public void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    writer.writeBooleanField(field.getJsonName(), value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) throws IOException {
    writer.writeNumberField(field.getJsonName(), enumValue.getEnumNumber());
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    writer.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeSInt32(ProtoFieldInfo field, int value) throws IOException {
    writer.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeint32(ProtoFieldInfo field, int value) throws IOException {
    writer.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeInt64(ProtoFieldInfo field, long value) throws IOException {
    writer.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64(ProtoFieldInfo field, long value) throws IOException {
    writer.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    writer.writeString(Long.toString(value));
  }

  @Override
  protected void writeUInt64Value(long value) throws IOException {
    writer.writeString(Long.toString(value));
  }

  @Override
  public void writeUInt64(ProtoFieldInfo field, long value) throws IOException {
    writer.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed32(ProtoFieldInfo field, int value) throws IOException {
    writer.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeDouble(ProtoFieldInfo field, double value) throws IOException {
    writer.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    writer.writeFieldName(field.getJsonName());
    // Marshalers encoded String into UTF-8 bytes to optimize for binary serialization where
    // we are able to avoid the encoding process happening twice, one for size computation and one
    // for actual writing. JsonGenerator actually has a writeUTF8String that would be able to accept
    // this, but it only works when writing to an OutputStream, but not to a String like we do for
    // writing to logs. It's wasteful to take a String, convert it to bytes, and convert back to
    // the same String but we can see if this can be improved in the future.
    writer.writeString(new String(utf8Bytes, StandardCharsets.UTF_8));
  }

  @Override
  public void writeString(
      ProtoFieldInfo field, String string, int utf8Length, MarshalerContext context)
      throws IOException {
    writer.writeFieldName(field.getJsonName());
    writer.writeString(string);
  }

  @Override
  public void writeRepeatedString(ProtoFieldInfo field, byte[][] utf8Bytes) throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
    for (byte[] value : utf8Bytes) {
      // Marshalers encoded String into UTF-8 bytes to optimize for binary serialization where
      // we are able to avoid the encoding process happening twice, one for size computation and one
      // for actual writing. JsonGenerator actually has a writeUTF8String that would be able to
      // accept
      // this, but it only works when writing to an OutputStream, but not to a String like we do for
      // writing to logs. It's wasteful to take a String, convert it to bytes, and convert back to
      // the same String but we can see if this can be improved in the future.
      writer.writeString(new String(value, StandardCharsets.UTF_8));
    }
    writer.writeEndArray();
  }

  @Override
  public void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    writer.writeBinaryField(field.getJsonName(), value);
  }

  @Override
  public void writeByteBuffer(ProtoFieldInfo field, ByteBuffer value) throws IOException {
    byte[] data = new byte[value.capacity()];
    ((ByteBuffer) value.duplicate().clear()).get(data);
    writer.writeBinaryField(field.getJsonName(), data);
  }

  @Override
  protected void writeStartMessage(ProtoFieldInfo field, int protoMessageSize) throws IOException {
    writer.writeObjectFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndMessage() throws IOException {
    writer.writeEndObject();
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedPrimitive() throws IOException {
    writer.writeEndArray();
  }

  @Override
  protected void writeStartRepeatedVarint(ProtoFieldInfo field, int payloadSize)
      throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedVarint() throws IOException {
    writer.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    writer.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    writer.writeEndArray();
  }

  @Override
  public <T> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context)
      throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
    for (int i = 0; i < messages.size(); i++) {
      T message = messages.get(i);
      writer.writeStartObject();
      marshaler.writeTo(this, message, context);
      writer.writeEndObject();
    }
    writer.writeEndArray();
  }

  @Override
  public void writeStartRepeated(ProtoFieldInfo field) throws IOException {
    writer.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  public void writeEndRepeated() throws IOException {
    writer.writeEndArray();
  }

  @Override
  public void writeStartRepeatedElement(ProtoFieldInfo field, int protoMessageSize)
      throws IOException {
    writer.writeStartObject();
  }

  @Override
  public void writeEndRepeatedElement() throws IOException {
    writer.writeEndObject();
  }

  // Not a field.
  void writeMessageValue(Marshaler message) throws IOException {
    writer.writeStartObject();
    message.writeTo(this);
    writer.writeEndObject();
  }

  @Override
  public void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException {
    writer.writeRaw(jsonSerialized);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
