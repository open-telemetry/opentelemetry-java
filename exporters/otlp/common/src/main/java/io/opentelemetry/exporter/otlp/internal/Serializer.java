package io.opentelemetry.exporter.otlp.internal;

import java.io.IOException;
import java.util.List;

public abstract class Serializer {

  public static Serializer createProtoSerializer(CodedOutputStream output) {
    return new ProtoSerializer(output);
  }

  Serializer() {}

  public void serializeBool(int protoFieldNumber, String jsonFieldName, boolean value)
      throws IOException {
    if (!value) {
      return;
    }
    writeBool(protoFieldNumber, jsonFieldName, value);
  }

  protected abstract void writeBool(int protoFieldNumber, String jsonFieldName, boolean value)
      throws IOException;

  public void serializeUint32(int protoFieldNumber, String jsonFieldName, int value)
      throws IOException {
    if (value == 0) {
      return;
    }
    writeUint32(protoFieldNumber, jsonFieldName, value);
  }

  protected abstract void writeUint32(int protoFieldNumber, String jsonFieldName, int value)
      throws IOException;

  public void serializeFixed64(int protoFieldNumber, String jsonFieldName, long value)
      throws IOException {
    if (value == 0) {
      return;
    }
    writeFixed64(protoFieldNumber, jsonFieldName, value);
  }

  protected abstract void writeFixed64(int protoFieldNumber, String jsonFieldName, long value)
      throws IOException;

  protected abstract void writeFixed64Value(long value) throws IOException;

  public void serializeDouble(int protoFieldNumber, String jsonFieldName, double value)
      throws IOException {
    if (value == 0D) {
      return;
    }
    writeDouble(protoFieldNumber, jsonFieldName, value);
  }

  protected abstract void writeDouble(int protoFieldNumber, String jsonFieldName, double value)
      throws IOException;

  protected abstract void writeDoubleValue(double value) throws IOException;

  public void serializeBytes(int protoFieldNumber, String jsonFieldName, byte[] value)
      throws IOException {
    if (value.length == 0) {
      return;
    }
    writeBytes(protoFieldNumber, jsonFieldName, value);
  }

  protected abstract void writeBytes(int protoFieldNumber, String jsonFieldName, byte[] value)
      throws IOException;

  protected abstract void writeStartMessage(
      int protoFieldNumber, String jsonFieldName, int protoMessageSize) throws IOException;

  protected abstract void writeEndMessage() throws IOException;

  public void serializeMessage(int protoFieldNumber, String jsonFieldName, Marshaler message)
      throws IOException {
    writeStartMessage(protoFieldNumber, jsonFieldName, message.getProtoSerializedSize());
    message.writeTo(this);
    writeEndMessage();
  }

  protected abstract void writeStartRepeatedPrimitive(
      int protoFieldNumber, String jsonFieldName, int protoSizePerElement, int numElements)
      throws IOException;

  protected abstract void writeEndRepeatedPrimitive() throws IOException;

  public void serializeRepeatedFixed64(
      int protoFieldNumber, String jsonFieldName, List<Long> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(
        protoFieldNumber, jsonFieldName, WireFormat.FIXED64_SIZE, values.size());
    for (long value : values) {
      writeFixed64Value(value);
    }
    writeEndRepeatedPrimitive();
  }

  public void serializeRepeatedDouble(
      int protoFieldNumber, String jsonFieldName, List<Double> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(
        protoFieldNumber, jsonFieldName, WireFormat.FIXED64_SIZE, values.size());
    for (double value : values) {
      writeDoubleValue(value);
    }
    writeEndRepeatedPrimitive();
  }

  public abstract void serializeRepeatedMessage(
      int protoFieldNumber, String jsonFieldName, Marshaler[] repeatedMessage)
      throws IOException;
}
