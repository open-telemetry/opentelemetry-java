/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

final class JsonSerializer extends Serializer {

  private final JsonBufferedEncoder encoder;

  JsonSerializer(OutputStream output) {
    this.encoder = new JsonBufferedEncoder(output);
  }

  @Override
  protected void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException {
    encoder.writeStringField(field.getJsonName(), traceId);
  }

  @Override
  protected void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException {
    encoder.writeStringField(field.getJsonName(), spanId);
  }

  @Override
  public void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    encoder.writeBooleanField(field.getJsonName(), value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) throws IOException {
    encoder.writeNumberField(field.getJsonName(), enumValue.getEnumNumber());
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    encoder.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeSInt32(ProtoFieldInfo field, int value) throws IOException {
    encoder.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeint32(ProtoFieldInfo field, int value) throws IOException {
    encoder.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeInt64(ProtoFieldInfo field, long value) throws IOException {
    encoder.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64(ProtoFieldInfo field, long value) throws IOException {
    encoder.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    encoder.writeString(Long.toString(value));
  }

  @Override
  protected void writeUInt64Value(long value) throws IOException {
    encoder.writeString(Long.toString(value));
  }

  @Override
  public void writeUInt64(ProtoFieldInfo field, long value) throws IOException {
    encoder.writeStringField(field.getJsonName(), Long.toString(value));
  }

  @Override
  protected void writeFixed32(ProtoFieldInfo field, int value) throws IOException {
    encoder.writeNumberField(field.getJsonName(), value);
  }

  @Override
  public void writeDouble(ProtoFieldInfo field, double value) throws IOException {
    encoder.writeNumberField(field.getJsonName(), value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    encoder.writeNumber(value);
  }

  @Override
  public void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    encoder.writeFieldName(field.getJsonName());
    // Marshalers already encoded the String to UTF-8 bytes (binary serialization needs them for
    // both size computation and writing), so write them directly rather than decoding and
    // re-encoding.
    encoder.writeUtf8String(utf8Bytes);
  }

  @Override
  public void writeString(
      ProtoFieldInfo field, String string, int utf8Length, MarshalerContext context)
      throws IOException {
    encoder.writeFieldName(field.getJsonName());
    encoder.writeString(string);
  }

  @Override
  public void writeRepeatedString(ProtoFieldInfo field, byte[][] utf8Bytes) throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
    for (byte[] value : utf8Bytes) {
      // See writeString(ProtoFieldInfo, byte[]): the bytes are already UTF-8, so write directly.
      encoder.writeUtf8String(value);
    }
    encoder.writeEndArray();
  }

  @Override
  public void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    encoder.writeBinaryField(field.getJsonName(), value);
  }

  @Override
  public void writeByteBuffer(ProtoFieldInfo field, ByteBuffer value) throws IOException {
    byte[] data = new byte[value.capacity()];
    ((ByteBuffer) value.duplicate().clear()).get(data);
    encoder.writeBinaryField(field.getJsonName(), data);
  }

  @Override
  protected void writeStartMessage(ProtoFieldInfo field, int protoMessageSize) throws IOException {
    encoder.writeObjectFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndMessage() throws IOException {
    encoder.writeEndObject();
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedPrimitive() throws IOException {
    encoder.writeEndArray();
  }

  @Override
  protected void writeStartRepeatedVarint(ProtoFieldInfo field, int payloadSize)
      throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  protected void writeEndRepeatedVarint() throws IOException {
    encoder.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    encoder.writeEndArray();
  }

  @Override
  public void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
    for (Marshaler marshaler : repeatedMessage) {
      writeMessageValue(marshaler);
    }
    encoder.writeEndArray();
  }

  @Override
  public <T> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context)
      throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
    for (int i = 0; i < messages.size(); i++) {
      T message = messages.get(i);
      encoder.writeStartObject();
      marshaler.writeTo(this, message, context);
      encoder.writeEndObject();
    }
    encoder.writeEndArray();
  }

  @Override
  public void writeStartRepeated(ProtoFieldInfo field) throws IOException {
    encoder.writeArrayFieldStart(field.getJsonName());
  }

  @Override
  public void writeEndRepeated() throws IOException {
    encoder.writeEndArray();
  }

  @Override
  public void writeStartRepeatedElement(ProtoFieldInfo field, int protoMessageSize)
      throws IOException {
    encoder.writeStartObject();
  }

  @Override
  public void writeEndRepeatedElement() throws IOException {
    encoder.writeEndObject();
  }

  // Not a field.
  void writeMessageValue(Marshaler message) throws IOException {
    encoder.writeStartObject();
    message.writeTo(this);
    encoder.writeEndObject();
  }

  @Override
  public void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException {
    encoder.writeRaw(jsonSerialized);
  }

  @Override
  public void close() throws IOException {
    encoder.close();
  }
}
