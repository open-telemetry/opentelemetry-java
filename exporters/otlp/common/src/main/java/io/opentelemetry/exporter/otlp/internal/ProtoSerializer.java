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
  protected void writeBool(int protoFieldNumber, String jsonFieldName, boolean value)
      throws IOException {
    output.writeBool(protoFieldNumber, value);
  }

  @Override
  protected void writeEnum(int protoFieldNumber, String jsonFieldName, int enumNumber)
      throws IOException {
    output.writeEnum(protoFieldNumber, enumNumber);
  }

  @Override
  protected void writeUint32(int protoFieldNumber, String jsonFieldName, int value)
      throws IOException {
    output.writeUInt32(protoFieldNumber, value);
  }

  @Override
  protected void writeInt64(int protoFieldNumber, String jsonFieldName, long value)
      throws IOException {
    output.writeInt64(protoFieldNumber, value);
  }

  @Override
  protected void writeFixed64(int protoFieldNumber, String jsonFieldName, long value)
      throws IOException {
    output.writeFixed64(protoFieldNumber, value);
  }

  @Override
  protected void writeFixed64Value(long value) throws IOException {
    output.writeFixed64NoTag(value);
  }

  @Override
  protected void writeDouble(int protoFieldNumber, String jsonFieldName, double value)
      throws IOException {
    output.writeDouble(protoFieldNumber, value);
  }

  @Override
  protected void writeDoubleValue(double value) throws IOException {
    output.writeDoubleNoTag(value);
  }

  @Override
  protected void writeString(int protoFieldNumber, String jsonFieldName, byte[] utf8Bytes)
      throws IOException {
    writeBytes(protoFieldNumber, jsonFieldName, utf8Bytes);
  }

  @Override
  protected void writeBytes(int protoFieldNumber, String jsonFieldName, byte[] value)
      throws IOException {
    output.writeByteArray(protoFieldNumber, value);
  }

  @Override
  protected void writeStartMessage(int protoFieldNumber, String jsonFieldName, int protoMessageSize)
      throws IOException {
    output.writeTag(protoFieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    output.writeUInt32NoTag(protoMessageSize);
  }

  @Override
  protected void writeEndMessage() {
    // Do nothing
  }

  @Override
  protected void writeStartRepeatedPrimitive(
      int protoFieldNumber, String jsonFieldName, int protoSizePerElement, int numElements)
      throws IOException {
    output.writeTag(protoFieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    output.writeUInt32NoTag(protoSizePerElement * numElements);
  }

  @Override
  protected void writeEndRepeatedPrimitive() {
    // Do nothing
  }

  @Override
  public void serializeRepeatedMessage(
      int protoFieldNumber, String jsonFieldName, Marshaler[] repeatedMessage) throws IOException {
    for (Marshaler message : repeatedMessage) {
      serializeMessage(protoFieldNumber, jsonFieldName, message);
    }
  }

  @Override
  public void serializeRepeatedMessage(
      int protoFieldNumber, String jsonFieldName, List<? extends Marshaler> repeatedMessage)
      throws IOException {
    for (Marshaler message : repeatedMessage) {
      serializeMessage(protoFieldNumber, jsonFieldName, message);
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
