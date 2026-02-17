/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Serializer to use when converting from an SDK data object into a protobuf output format. Unlike
 * {@link CodedOutputStream}, which strictly encodes data into the protobuf binary format, this
 *
 * <ul>
 *   <li>Handles proto3 semantics of not outputting the value when it matches the default of a field
 *   <li>Can be implemented to serialize into protobuf JSON format (not binary)
 * </ul>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class Serializer implements AutoCloseable {
  private static final MarshalerContext.Key ATTRIBUTES_WRITER_KEY = MarshalerContext.key();

  Serializer() {}

  /** Serializes a trace ID field. */
  public void serializeTraceId(ProtoFieldInfo field, @Nullable String traceId) throws IOException {
    if (traceId == null) {
      return;
    }
    writeTraceId(field, traceId);
  }

  public void serializeTraceId(
      ProtoFieldInfo field, @Nullable String traceId, MarshalerContext context) throws IOException {
    if (traceId == null) {
      return;
    }
    writeTraceId(field, traceId, context);
  }

  protected abstract void writeTraceId(ProtoFieldInfo field, String traceId) throws IOException;

  protected void writeTraceId(ProtoFieldInfo field, String traceId, MarshalerContext context)
      throws IOException {
    writeTraceId(field, traceId);
  }

  /** Serializes a span ID field. */
  public void serializeSpanId(ProtoFieldInfo field, @Nullable String spanId) throws IOException {
    if (spanId == null) {
      return;
    }
    writeSpanId(field, spanId);
  }

  public void serializeSpanId(
      ProtoFieldInfo field, @Nullable String spanId, MarshalerContext context) throws IOException {
    if (spanId == null) {
      return;
    }
    writeSpanId(field, spanId, context);
  }

  protected abstract void writeSpanId(ProtoFieldInfo field, String spanId) throws IOException;

  protected void writeSpanId(ProtoFieldInfo field, String spanId, MarshalerContext context)
      throws IOException {
    writeSpanId(field, spanId);
  }

  /** Serializes a protobuf {@code bool} field. */
  public void serializeBool(ProtoFieldInfo field, boolean value) throws IOException {
    if (!value) {
      return;
    }
    writeBool(field, value);
  }

  /** Writes a protobuf {@code bool} field, even if it matches the default value. */
  public abstract void writeBool(ProtoFieldInfo field, boolean value) throws IOException;

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

  /** Serializes a protobuf {@code sint32} field. */
  public void serializeSInt32(ProtoFieldInfo field, int value) throws IOException {
    if (value == 0) {
      return;
    }
    writeSInt32(field, value);
  }

  protected abstract void writeSInt32(ProtoFieldInfo info, int value) throws IOException;

  /** Serializes a protobuf {@code int32} field. */
  public void serializeInt32(ProtoFieldInfo field, int value) throws IOException {
    if (value == 0) {
      return;
    }
    writeint32(field, value);
  }

  /** Serializes a protobuf {@code int32} field. */
  public void serializeInt32Optional(ProtoFieldInfo field, int value) throws IOException {
    writeint32(field, value);
  }

  /** Serializes a protobuf {@code int32} field. */
  public void serializeInt32Optional(ProtoFieldInfo field, @Nullable Integer value)
      throws IOException {
    if (value != null) {
      serializeInt32Optional(field, (int) value);
    }
  }

  protected abstract void writeint32(ProtoFieldInfo field, int value) throws IOException;

  /** Serializes a protobuf {@code int64} field. */
  public void serializeInt64(ProtoFieldInfo field, long value) throws IOException {
    if (value == 0) {
      return;
    }
    writeInt64(field, value);
  }

  /** Serializes a protobuf {@code uint64} field. */
  public void serializeUInt64(ProtoFieldInfo field, long value) throws IOException {
    if (value == 0) {
      return;
    }
    writeUInt64(field, value);
  }

  /** Writes a protobuf {@code int64} field, even if it matches the default value. */
  public abstract void writeInt64(ProtoFieldInfo field, long value) throws IOException;

  /** Writes a protobuf {@code uint64} field, even if it matches the default value. */
  public abstract void writeUInt64(ProtoFieldInfo field, long value) throws IOException;

  /** Serializes a protobuf {@code fixed64} field. */
  public void serializeFixed64(ProtoFieldInfo field, long value) throws IOException {
    if (value == 0) {
      return;
    }
    writeFixed64(field, value);
  }

  /** Serializes a protobuf {@code fixed64} field. */
  public void serializeFixed64Optional(ProtoFieldInfo field, long value) throws IOException {
    writeFixed64(field, value);
  }

  protected abstract void writeFixed64(ProtoFieldInfo field, long value) throws IOException;

  protected abstract void writeFixed64Value(long value) throws IOException;

  protected abstract void writeUInt64Value(long value) throws IOException;

  /**
   * Serializes a byte as a protobuf {@code fixed32} field. Ensures that there is no sign
   * propagation if the high bit in the byte is set.
   */
  public void serializeByteAsFixed32(ProtoFieldInfo field, byte value) throws IOException {
    serializeFixed32(field, ((int) value) & 0xff);
  }

  /** Serializes a protobuf {@code fixed32} field. */
  public void serializeFixed32(ProtoFieldInfo field, int value) throws IOException {
    if (value == 0) {
      return;
    }
    writeFixed32(field, value);
  }

  protected abstract void writeFixed32(ProtoFieldInfo field, int value) throws IOException;

  /** Serializes a proto buf {@code double} field. */
  public void serializeDouble(ProtoFieldInfo field, double value) throws IOException {
    if (value == 0D) {
      return;
    }
    writeDouble(field, value);
  }

  /** Serializes a proto buf {@code double} field. */
  public void serializeDoubleOptional(ProtoFieldInfo field, double value) throws IOException {
    writeDouble(field, value);
  }

  /** Writes a protobuf {@code double} field, even if it matches the default value. */
  public abstract void writeDouble(ProtoFieldInfo field, double value) throws IOException;

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

  /**
   * Serializes a protobuf {@code repeated string} field. {@code utf8Bytes} is the UTF8 encoded
   * bytes of the strings to serialize.
   */
  @SuppressWarnings("AvoidObjectArrays")
  public void serializeRepeatedString(ProtoFieldInfo field, byte[][] utf8Bytes) throws IOException {
    if (utf8Bytes.length == 0) {
      return;
    }
    writeRepeatedString(field, utf8Bytes);
  }

  /**
   * Serializes a protobuf {@code string} field. {@code string} is the value to be serialized and
   * {@code utf8Length} is the length of the string after it is encoded in UTF8. This method reads
   * elements from context, use together with {@link
   * StatelessMarshalerUtil#sizeStringWithContext(ProtoFieldInfo, String, MarshalerContext)}.
   */
  public void serializeStringWithContext(
      ProtoFieldInfo field, @Nullable String string, MarshalerContext context) throws IOException {
    if (string == null || string.isEmpty()) {
      return;
    }
    writeStringWithContext(field, string, context);
  }

  /**
   * Writes a protobuf {@code string} field, even if it matches the default value. This method reads
   * elements from context, use together with {@link StatelessMarshalerUtil#getUtf8Size(String,
   * MarshalerContext)}.
   */
  public void writeStringWithContext(ProtoFieldInfo field, String string, MarshalerContext context)
      throws IOException {
    if (context.marshalStringNoAllocation()) {
      writeString(field, string, context.getSize(), context);
    } else {
      byte[] valueUtf8 = context.getData(byte[].class);
      writeString(field, valueUtf8);
    }
  }

  /** Writes a protobuf {@code string} field, even if it matches the default value. */
  public abstract void writeString(ProtoFieldInfo field, byte[] utf8Bytes) throws IOException;

  public abstract void writeString(
      ProtoFieldInfo field, String string, int utf8Length, MarshalerContext context)
      throws IOException;

  /** Writes a protobuf {@code repeated string} field, even if it matches the default value. */
  @SuppressWarnings("AvoidObjectArrays")
  public abstract void writeRepeatedString(ProtoFieldInfo field, byte[][] utf8Bytes)
      throws IOException;

  /** Serializes a protobuf {@code bytes} field. */
  public void serializeBytes(ProtoFieldInfo field, byte[] value) throws IOException {
    if (value.length == 0) {
      return;
    }
    writeBytes(field, value);
  }

  /**
   * Serializes a protobuf {@code bytes} field. Writes all content of the ByteBuffer regardless of
   * the current position and limit. Does not alter the position or limit of the provided
   * ByteBuffer.
   */
  public void serializeByteBuffer(ProtoFieldInfo field, ByteBuffer value) throws IOException {
    if (value.capacity() == 0) {
      return;
    }
    writeByteBuffer(field, value);
  }

  public abstract void writeBytes(ProtoFieldInfo field, byte[] value) throws IOException;

  public abstract void writeByteBuffer(ProtoFieldInfo field, ByteBuffer value) throws IOException;

  protected abstract void writeStartMessage(ProtoFieldInfo field, int protoMessageSize)
      throws IOException;

  protected abstract void writeEndMessage() throws IOException;

  /** Serializes a protobuf embedded {@code message}. */
  public void serializeMessage(ProtoFieldInfo field, Marshaler message) throws IOException {
    writeStartMessage(field, message.getBinarySerializedSize());
    message.writeTo(this);
    writeEndMessage();
  }

  /**
   * Serializes a protobuf embedded {@code message}. This method adds elements to context, use
   * together with {@link StatelessMarshalerUtil#sizeMessageWithContext(ProtoFieldInfo, Object,
   * StatelessMarshaler, MarshalerContext)}.
   */
  public <T> void serializeMessageWithContext(
      ProtoFieldInfo field, T message, StatelessMarshaler<T> marshaler, MarshalerContext context)
      throws IOException {
    writeStartMessage(field, context.getSize());
    marshaler.writeTo(this, message, context);
    writeEndMessage();
  }

  /**
   * Serializes a protobuf embedded {@code message}. This method adds elements to context, use
   * together with {@link StatelessMarshalerUtil#sizeMessageWithContext(ProtoFieldInfo, Object,
   * Object, StatelessMarshaler2, MarshalerContext)}.
   */
  public <K, V> void serializeMessageWithContext(
      ProtoFieldInfo field,
      K key,
      V value,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context)
      throws IOException {
    writeStartMessage(field, context.getSize());
    marshaler.writeTo(this, key, value, context);
    writeEndMessage();
  }

  @SuppressWarnings("SameParameterValue")
  protected abstract void writeStartRepeatedPrimitive(
      ProtoFieldInfo field, int protoSizePerElement, int numElements) throws IOException;

  protected abstract void writeEndRepeatedPrimitive() throws IOException;

  protected abstract void writeStartRepeatedVarint(ProtoFieldInfo field, int payloadSize)
      throws IOException;

  protected abstract void writeEndRepeatedVarint() throws IOException;

  /** Serializes a {@code repeated int32} field. */
  public void serializeRepeatedInt32(ProtoFieldInfo field, List<Integer> values)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }

    int payloadSize = 0;
    for (int v : values) {
      payloadSize += CodedOutputStream.computeInt32SizeNoTag(v);
    }

    writeStartRepeatedVarint(field, payloadSize);
    for (int value : values) {
      writeUInt64Value(value);
    }
    writeEndRepeatedVarint();
  }

  /** Serializes a {@code repeated fixed64} field. */
  public void serializeRepeatedFixed64(ProtoFieldInfo field, List<Long> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(field, WireFormat.FIXED64_SIZE, values.size());
    for (int i = 0; i < values.size(); i++) {
      Long value = values.get(i);
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

  /** Serializes a {@code repeated uint64} field. */
  public void serializeRepeatedUInt64(ProtoFieldInfo field, long[] values) throws IOException {
    if (values.length == 0) {
      return;
    }

    int payloadSize = 0;
    for (long v : values) {
      payloadSize += CodedOutputStream.computeUInt64SizeNoTag(v);
    }

    writeStartRepeatedVarint(field, payloadSize);
    for (long value : values) {
      writeUInt64Value(value);
    }
    writeEndRepeatedVarint();
  }

  /** Serializes a {@code repeated uint64} field. */
  public void serializeRepeatedUInt64(ProtoFieldInfo field, List<Long> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }

    int payloadSize = 0;
    for (long v : values) {
      payloadSize += CodedOutputStream.computeUInt64SizeNoTag(v);
    }

    writeStartRepeatedVarint(field, payloadSize);
    for (long value : values) {
      writeUInt64Value(value);
    }
    writeEndRepeatedVarint();
  }

  /**
   * Serializes a {@code repeated uint64} field.
   *
   * <p>NOTE: This is the same as {@link #serializeRepeatedUInt64(ProtoFieldInfo, long[])} but
   * instead of taking a primitive array it takes a {@link DynamicPrimitiveLongList} as input.
   */
  public void serializeRepeatedUInt64(ProtoFieldInfo field, DynamicPrimitiveLongList values)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }

    int payloadSize = 0;
    for (int i = 0; i < values.size(); i++) {
      long v = values.getLong(i);
      payloadSize += CodedOutputStream.computeUInt64SizeNoTag(v);
    }

    writeStartRepeatedVarint(field, payloadSize);
    for (int i = 0; i < values.size(); i++) {
      long value = values.getLong(i);
      writeUInt64Value(value);
    }
    writeEndRepeatedVarint();
  }

  /** Serializes a {@code repeated int64} field. */
  public void serializeRepeatedInt64(ProtoFieldInfo field, List<Long> values) throws IOException {
    if (values.isEmpty()) {
      return;
    }

    int payloadSize = 0;
    for (long v : values) {
      payloadSize += CodedOutputStream.computeInt64SizeNoTag(v);
    }

    writeStartRepeatedVarint(field, payloadSize);
    for (long value : values) {
      writeUInt64Value(value);
    }
    writeEndRepeatedVarint();
  }

  /** Serializes a {@code repeated double} field. */
  public void serializeRepeatedDouble(ProtoFieldInfo field, List<Double> values)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }
    writeStartRepeatedPrimitive(field, WireFormat.FIXED64_SIZE, values.size());
    for (int i = 0; i < values.size(); i++) {
      Double value = values.get(i);
      writeDoubleValue(value);
    }
    writeEndRepeatedPrimitive();
  }

  /** Serializes {@code repeated message} field. */
  @SuppressWarnings("AvoidObjectArrays")
  public abstract void serializeRepeatedMessage(ProtoFieldInfo field, Marshaler[] repeatedMessage)
      throws IOException;

  /** Serializes {@code repeated message} field. */
  public abstract void serializeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) throws IOException;

  /**
   * Serializes {@code repeated message} field. This method reads elements from context, use
   * together with {@link StatelessMarshalerUtil#sizeRepeatedMessageWithContext(ProtoFieldInfo,
   * List, StatelessMarshaler, MarshalerContext)}.
   */
  public abstract <T> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context)
      throws IOException;

  /**
   * Serializes {@code repeated message} field. This method reads elements from context, use
   * together with {@link StatelessMarshalerUtil#sizeRepeatedMessageWithContext(ProtoFieldInfo,
   * Collection, StatelessMarshaler, MarshalerContext, MarshalerContext.Key)}.
   */
  @SuppressWarnings("unchecked")
  public <T> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Collection<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key)
      throws IOException {
    if (messages instanceof List) {
      serializeRepeatedMessageWithContext(field, (List<T>) messages, marshaler, context);
      return;
    }

    writeStartRepeated(field);

    if (!messages.isEmpty()) {
      RepeatedElementWriter<T> writer = context.getInstance(key, RepeatedElementWriter::new);
      writer.initialize(field, this, marshaler, context);
      try {
        messages.forEach(writer);
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    }

    writeEndRepeated();
  }

  /**
   * Serializes {@code repeated message} field. This method reads elements from context, use
   * together with {@link StatelessMarshalerUtil#sizeRepeatedMessageWithContext(ProtoFieldInfo, Map,
   * StatelessMarshaler2, MarshalerContext, MarshalerContext.Key)}.
   */
  public <K, V> void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Map<K, V> messages,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key)
      throws IOException {
    writeStartRepeated(field);

    if (!messages.isEmpty()) {
      RepeatedElementPairWriter<K, V> writer =
          context.getInstance(key, RepeatedElementPairWriter::new);
      writer.initialize(field, this, marshaler, context);
      try {
        messages.forEach(writer);
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    }

    writeEndRepeated();
  }

  /**
   * Serializes {@code repeated message} field. This method reads elements from context, use
   * together with {@link StatelessMarshalerUtil#sizeRepeatedMessageWithContext(ProtoFieldInfo,
   * Attributes, StatelessMarshaler2, MarshalerContext)}.
   */
  public void serializeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Attributes attributes,
      StatelessMarshaler2<AttributeKey<?>, Object> marshaler,
      MarshalerContext context)
      throws IOException {
    writeStartRepeated(field);

    if (!attributes.isEmpty()) {
      RepeatedElementPairWriter<AttributeKey<?>, Object> writer =
          context.getInstance(ATTRIBUTES_WRITER_KEY, RepeatedElementPairWriter::new);
      writer.initialize(field, this, marshaler, context);
      try {
        attributes.forEach(writer);
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    }

    writeEndRepeated();
  }

  private static class RepeatedElementWriter<T> implements Consumer<T> {
    @SuppressWarnings("NullAway")
    private ProtoFieldInfo field;

    @SuppressWarnings("NullAway")
    private Serializer output;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler<T> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field,
        Serializer output,
        StatelessMarshaler<T> marshaler,
        MarshalerContext context) {
      this.field = field;
      this.output = output;
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(T element) {
      try {
        output.writeStartRepeatedElement(field, context.getSize());
        marshaler.writeTo(output, element, context);
        output.writeEndRepeatedElement();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private static class RepeatedElementPairWriter<K, V> implements BiConsumer<K, V> {
    @SuppressWarnings("NullAway")
    private ProtoFieldInfo field;

    @SuppressWarnings("NullAway")
    private Serializer output;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler2<K, V> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field,
        Serializer output,
        StatelessMarshaler2<K, V> marshaler,
        MarshalerContext context) {
      this.field = field;
      this.output = output;
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(K key, V value) {
      try {
        output.writeStartRepeatedElement(field, context.getSize());
        marshaler.writeTo(output, key, value, context);
        output.writeEndRepeatedElement();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /** Writes start of repeated messages. */
  public abstract void writeStartRepeated(ProtoFieldInfo field) throws IOException;

  /** Writes end of repeated messages. */
  public abstract void writeEndRepeated() throws IOException;

  /** Writes start of a repeated message element. */
  public abstract void writeStartRepeatedElement(ProtoFieldInfo field, int protoMessageSize)
      throws IOException;

  /** Writes end of a repeated message element. */
  public abstract void writeEndRepeatedElement() throws IOException;

  /** Writes the value for a message field that has been pre-serialized. */
  public abstract void writeSerializedMessage(byte[] protoSerialized, String jsonSerialized)
      throws IOException;

  @Override
  public abstract void close() throws IOException;
}
