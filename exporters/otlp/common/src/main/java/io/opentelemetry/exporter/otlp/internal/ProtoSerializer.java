/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import java.io.IOException;
import java.util.List;

/** Serializer for the protobuf binary wire format. */
final class ProtoSerializer extends Serializer {

  private final CodedOutputStream output;

  ProtoSerializer(CodedOutputStream output) {
    this.output = output;
  }

  @Override
  protected void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeBoolNoTag(value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, int enumNumber) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeEnumNoTag(enumNumber);
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeUInt32NoTag(value);
  }

  @Override
  protected void writeInt64(ProtoFieldInfo field, long value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeInt64NoTag(value);
  }

  @Override
  protected void writeFixed64(ProtoFieldInfo field, long value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeFixed64NoTag(value);
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    output.writeFixed64NoTag(value);
  }

  @Override
  protected void writeDouble(ProtoFieldInfo field, double value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeDoubleNoTag(value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    output.writeDoubleNoTag(value);
  }

  @Override
  protected void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    writeBytes(field, utf8Bytes);
  }

  @Override
  protected void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeByteArrayNoTag(value);
  }

  @Override
  protected void writeStartMessage(ProtoFieldInfo field, int protoMessageSize) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeUInt32NoTag(protoMessageSize);
  }

  @Override
  protected void writeEndMessage() {
    // Do nothing
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeUInt32NoTag(protoSizePerElement * numElements);
  }

  @Override
  protected void writeEndRepeatedPrimitive() {
    // Do nothing
  }

  @Override
  public void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException {
    for (Marshaler message : repeatedMessage) {
      serializeMessage(field, message);
    }
  }

  @Override
  public void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException {
    for (Marshaler message : repeatedMessage) {
      serializeMessage(field, message);
    }
  }

  @Override
  public void writeSerializedMessage(byte[] protoSerialized, byte[] jsonSerialized)
      throws IOException {
    output.writeRawBytes(protoSerialized);
  }

  // TODO(anuraaga): Remove after moving protobuf Value serialization from AttributeMarshaler to
  // here.
  CodedOutputStream getCodedOutputStream() {
    return output;
  }
}
