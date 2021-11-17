/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Serializer to use when converting from an SDK data object into a protobuf output format. Unlike
 * {@link CodedOutputStream}, which strictly encodes data into the protobuf binary format, this
 *
 * <ul>
 *   <li>Handles proto3 semantics of not outputting the value when it matches the default of a field
 *   <li>Can be implemented to serialize into protobuf JSON format (not binary)
 * </ul>
 */
public abstract class Serializer implements AutoCloseable {

  Serializer() {}

  /** Serializes a trace ID field. */
  public void serializeTraceId(ProtoFieldInfo field, @Nullable String traceId) throws IOException {
    if (traceId == null) {
      return;
    }
    writeTraceId(field, traceId);
  }

  protected abstract void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException;

  /** Serializes a span ID field. */
  public void serializeSpanId(ProtoFieldInfo field, @Nullable String spanId) throws IOException {
    if (spanId == null) {
      return;
    }
    writeSpanId(field, spanId);
  }

  protected abstract void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException;

  /** Serializes a protobuf {@code bool} field. */
  public void serializeBool(ProtoFieldInfo field, boolean value) throws IOException {
    if (!value) {
      return;
    }
    writeBool(field, value);
  }

  protected abstract void writeBool(ProtoFieldInfo field, boolean value) throws IOException;

  /** Serializes a protobuf {@code enum} field. */
  public void serializeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) throws IOException {
    if (enumValue.getEnumNumber() == 0) {
      return;
    }
    writeEnum(field, enumValue);
  }

  protected abstract void writeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue)
      throws IOException;

  /** Serializes a protobuf {@code uint32} field. */
  public void serializeUInt32(ProtoFieldInfo field, int value) throws IOException {
    if (value == 0) {
      return;
    }
    writeUint32(field, value);
  }

  protected abstract void writeUint32(ProtoFieldInfo field, int value) throws IOException;

  /** Serializes a protobuf {@code uint32} field. */
  public void serializeInt32(ProtoFieldInfo field, int value) throws IOException {
    if (value == 0) {
      return;
    }
    writeint32(field, value);
  }

  protected abstract void writeint32(ProtoFieldInfo field, int value) throws IOException;

  /** Serializes a protobuf {@code int64} field. */
  public void serializeInt64(ProtoFieldInfo field, long value) throws IOException {
    if (value == 0) {
      return;
    }
    writeInt64(field, value);
  }

  protected abstract void writeInt64(ProtoFieldInfo field, long value) throws IOException;

  /** Serializes a protobuf {@code fixed64} field. */
  public void serializeFixed64(ProtoFieldInfo field, long value) throws IOException {
    serializeFixed64(field, value, /* force= */ false);
  }

  /** Serializes a protobuf {@code fixed64} field. */
  public void serializeFixed64(ProtoFieldInfo field, long value, boolean force) throws IOException {
    if (!force && value == 0) {
      return;
    }
    writeFixed64(field, value);
  }

  protected abstract void writeFixed64(ProtoFieldInfo field, long value) throws IOException;

  protected abstract void writeFixed64Value(long value) throws IOException;

  /** Serializes a protobuf {@code fixed32} field. */
  public void serializeFixed32(ProtoFieldInfo field, int value) throws IOException {
    serializeFixed32(field, value, /* force= */ false);
  }

  /** Serializes a protobuf {@code fixed32} field. */
  public void serializeFixed32(ProtoFieldInfo field, int value, boolean force) throws IOException {
    if (!force && value == 0) {
      return;
    }
    writeFixed32(field, value);
  }

  protected abstract void writeFixed32(ProtoFieldInfo field, int value) throws IOException;

  /** Serializes a proto buf {@code double} field. */
  public void serializeDouble(ProtoFieldInfo field, double value) throws IOException {
    serializeDouble(field, value, /* force= */ false);
  }
  /** Serializes a proto buf {@code double} field. */
  public void serializeDouble(ProtoFieldInfo field, double value, boolean force)
      throws IOException {
    if (!force && value == 0D) {
      return;
    }
    writeDouble(field, value);
  }

  protected abstract void writeDouble(ProtoFieldInfo field, double value) throws IOException;

  protected abstract void writeDoubleValue(double value) throws IOException;

  /**
   * Serializes a protobuf {@code string} field. {@code utf8Bytes} is the UTF8 encoded bytes of the
   * string to serialize.
   */
  public void serializeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException {
    if (utf8Bytes.length == 0) {
      return;
    }
    writeString(field, utf8Bytes);
  }

  protected abstract void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException;

  /** Serializes a protobuf {@code bytes} field. */
  public void serializeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    if (value.length == 0) {
      return;
    }
    writeBytes(field, value);
  }

  protected abstract void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException;

  protected abstract void writeStartMessage(ProtoFieldInfo field, int protoMessageSize)
      throws IOException;

  protected abstract void writeEndMessage() throws IOException;

  /** Serializes a protobuf embedded {@code message}. */
  public void serializeMessage(ProtoFieldInfo field, Marshaler message) throws IOException {
    writeStartMessage(field, message.getBinarySerializedSize());
    message.writeTo(this);
    writeEndMessage();
  }

  protected abstract void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException;

  protected abstract void writeEndRepeatedPrimitive() throws IOException;

  /** Serializes a {@code repeated fixed64} field. */
  public void serializeRepeatedFixed64(ProtoFieldInfo field, List<Long> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(field, WireFormat.FIXED64_SIZE, values.size());
    for (long value : values) {
      writeFixed64Value(value);
    }
    writeEndRepeatedPrimitive();
  }

  /** Serializes a {@code repeated fixed64} field. */
  public void serializeRepeatedFixed64(ProtoFieldInfo field, long[] values) throws IOException {
    if (values.length == 0) {
      return;
    }
    writeStartRepeatedPrimitive(field, WireFormat.FIXED64_SIZE, values.length);
    for (long value : values) {
      writeFixed64Value(value);
    }
    writeEndRepeatedPrimitive();
  }

  /** Serializes a {@code repeated double} field. */
  public void serializeRepeatedDouble(ProtoFieldInfo field, List<Double> values)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(field, WireFormat.FIXED64_SIZE, values.size());
    for (double value : values) {
      writeDoubleValue(value);
    }
    writeEndRepeatedPrimitive();
  }

  /** Serializes {@code repeated message} field. */
  public abstract void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException;

  /** Serializes {@code repeated message} field. */
  public abstract void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException;

  /** Writes the value for a message field that has been pre-serialized. */
  public abstract void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException;

  @Override
  public abstract void close() throws IOException;
}
