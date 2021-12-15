/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Serializer for the protobuf binary wire format. */
final class ProtoSerializer extends Serializer implements AutoCloseable {

  // Cache ID conversion to bytes since we know it's common to use the same ID multiple times within
  // a single export (trace ID and parent span ID).
  // In practice, there is often only one thread that calls this code in the BatchSpanProcessor so
  // reusing buffers for the thread is almost free. Even with multiple threads, it should still be
  // worth it and is common practice in serialization libraries such as Jackson.
  private static final ThreadLocal<Map<String, byte[]>> THREAD_LOCAL_ID_CACHE = new ThreadLocal<>();

  private final CodedOutputStream output;
  private final Map<String, byte[]> idCache;

  ProtoSerializer(OutputStream output) {
    this.output = CodedOutputStream.newInstance(output);
    idCache = getIdCache();
  }

  @Override
  protected void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException {
    byte[] traceIdBytes =
        idCache.computeIfAbsent(
            traceId, id -> OtelEncodingUtils.bytesFromBase16(id, TraceId.getLength()));
    writeBytes(field, traceIdBytes);
  }

  @Override
  protected void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException {
    byte[] spanIdBytes =
        idCache.computeIfAbsent(
            spanId, id -> OtelEncodingUtils.bytesFromBase16(id, SpanId.getLength()));
    writeBytes(field, spanIdBytes);
  }

  @Override
  protected void writeBool(ProtoFieldInfo field, boolean value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeBoolNoTag(value);
  }

  @Override
  protected void writeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeEnumNoTag(enumValue.getEnumNumber());
  }

  @Override
  protected void writeUint32(ProtoFieldInfo field, int value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeUInt32NoTag(value);
  }

  @Override
  protected void writeSInt32(ProtoFieldInfo field, int value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeSInt32NoTag(value);
  }

  @Override
  protected void writeint32(ProtoFieldInfo field, int value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeInt32NoTag(value);
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
  protected void writeUInt64Value(long value) throws IOException {
    output.writeUInt64NoTag(value);
  }

  @Override
  protected void writeFixed32(ProtoFieldInfo field, int value) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeFixed32NoTag(value);
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
  protected void writeStartRepeatedVarint(ProtoFieldInfo field, int payloadSize) throws IOException {
    output.writeUInt32NoTag(field.getTag());
    output.writeUInt32NoTag(payloadSize);
  }

  @Override
  protected void writeEndRepeatedVarint() {
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
  public void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException {
    output.writeRawBytes(protoSerialized);
  }

  @Override
  public void close() throws IOException {
    output.flush();
    idCache.clear();
  }

  private static Map<String, byte[]> getIdCache() {
    Map<String, byte[]> result = THREAD_LOCAL_ID_CACHE.get();
    if (result == null) {
      result = new HashMap<>();
      THREAD_LOCAL_ID_CACHE.set(result);
    }
    return result;
  }
}
