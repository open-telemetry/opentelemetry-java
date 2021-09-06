/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.ArrayValue;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

final class KeyValueMarshaler extends MarshalerWithSize {
  private static final KeyValueMarshaler[] EMPTY_REPEATED = new KeyValueMarshaler[0];

  static KeyValueMarshaler[] createRepeated(Attributes attributes) {
    if (attributes.isEmpty()) {
      return EMPTY_REPEATED;
    }

    KeyValueMarshaler[] attributeMarshalers = new KeyValueMarshaler[attributes.size()];
    attributes.forEach(
        new BiConsumer<AttributeKey<?>, Object>() {
          int index = 0;

          @Override
          public void accept(AttributeKey<?> attributeKey, Object o) {
            attributeMarshalers[index++] = KeyValueMarshaler.create(attributeKey, o);
          }
        });
    return attributeMarshalers;
  }

  private final byte[] keyUtf8;
  private final Marshaler value;

  @SuppressWarnings("unchecked")
  static KeyValueMarshaler create(AttributeKey<?> attributeKey, Object value) {
    byte[] keyUtf8 = MarshalerUtil.toBytes(attributeKey.getKey());
    switch (attributeKey.getType()) {
      case STRING:
        return new KeyValueMarshaler(
            keyUtf8, new AnyStringFieldMarshaler(MarshalerUtil.toBytes((String) value)));
      case LONG:
        return new KeyValueMarshaler(keyUtf8, new AnyInt64FieldMarshaler((long) value));
      case BOOLEAN:
        return new KeyValueMarshaler(keyUtf8, new AnyBoolFieldMarshaler((boolean) value));
      case DOUBLE:
        return new KeyValueMarshaler(keyUtf8, new AnyDoubleFieldMarshaler((double) value));
      case STRING_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8,
            new AnyArrayFieldMarshaler(ArrayValueMarshaler.createString((List<String>) value)));
      case LONG_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8,
            new AnyArrayFieldMarshaler(ArrayValueMarshaler.createInt64((List<Long>) value)));
      case BOOLEAN_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8,
            new AnyArrayFieldMarshaler(ArrayValueMarshaler.createBool((List<Boolean>) value)));
      case DOUBLE_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8,
            new AnyArrayFieldMarshaler(ArrayValueMarshaler.createDouble((List<Double>) value)));
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private KeyValueMarshaler(byte[] keyUtf8, Marshaler value) {
    super(calculateSize(keyUtf8, value));
    this.keyUtf8 = keyUtf8;
    this.value = value;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(KeyValue.KEY, keyUtf8);
    output.serializeMessage(KeyValue.VALUE, value);
  }

  private static int calculateSize(byte[] keyUtf8, Marshaler value) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, value);
    return size;
  }

  private static class AnyStringFieldMarshaler extends MarshalerWithSize {

    private final byte[] valueUtf8;

    AnyStringFieldMarshaler(byte[] valueUtf8) {
      super(calculateSize(valueUtf8));
      this.valueUtf8 = valueUtf8;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value is empty since it's a oneof.
      output.writeString(AnyValue.STRING_VALUE, valueUtf8);
    }

    private static int calculateSize(byte[] valueUtf8) {
      return AnyValue.STRING_VALUE.getTagSize()
          + CodedOutputStream.computeByteArraySizeNoTag(valueUtf8);
    }
  }

  private static class AnyBoolFieldMarshaler extends MarshalerWithSize {

    private final boolean value;

    AnyBoolFieldMarshaler(boolean value) {
      super(calculateSize(value));
      this.value = value;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value is empty since it's a oneof.
      output.writeBool(AnyValue.BOOL_VALUE, value);
    }

    private static int calculateSize(boolean value) {
      return AnyValue.BOOL_VALUE.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
    }
  }

  private static class AnyInt64FieldMarshaler extends MarshalerWithSize {

    private final long value;

    AnyInt64FieldMarshaler(long value) {
      super(calculateSize(value));
      this.value = value;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value is empty since it's a oneof.
      output.writeInt64(AnyValue.INT_VALUE, value);
    }

    private static int calculateSize(long value) {
      return AnyValue.INT_VALUE.getTagSize() + CodedOutputStream.computeInt64SizeNoTag(value);
    }
  }

  private static class AnyDoubleFieldMarshaler extends MarshalerWithSize {

    private final double value;

    AnyDoubleFieldMarshaler(double value) {
      super(calculateSize(value));
      this.value = value;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value is empty since it's a oneof.
      output.writeDouble(AnyValue.DOUBLE_VALUE, value);
    }

    private static int calculateSize(double value) {
      return AnyValue.DOUBLE_VALUE.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
    }
  }

  private static class AnyArrayFieldMarshaler extends MarshalerWithSize {
    private final Marshaler value;

    private AnyArrayFieldMarshaler(Marshaler value) {
      super(calculateSize(value));
      this.value = value;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeMessage(AnyValue.ARRAY_VALUE, value);
    }

    private static int calculateSize(Marshaler value) {
      return MarshalerUtil.sizeMessage(AnyValue.ARRAY_VALUE, value);
    }
  }

  private static class ArrayValueMarshaler extends MarshalerWithSize {

    static ArrayValueMarshaler createString(List<String> values) {
      int len = values.size();
      Marshaler[] marshalers = new AnyStringFieldMarshaler[len];
      for (int i = 0; i < len; i++) {
        marshalers[i] = new AnyStringFieldMarshaler(values.get(i).getBytes(StandardCharsets.UTF_8));
      }
      return new ArrayValueMarshaler(marshalers);
    }

    static ArrayValueMarshaler createBool(List<Boolean> values) {
      int len = values.size();
      Marshaler[] marshalers = new AnyBoolFieldMarshaler[len];
      for (int i = 0; i < len; i++) {
        marshalers[i] = new AnyBoolFieldMarshaler(values.get(i));
      }
      return new ArrayValueMarshaler(marshalers);
    }

    static ArrayValueMarshaler createInt64(List<Long> values) {
      int len = values.size();
      Marshaler[] marshalers = new AnyInt64FieldMarshaler[len];
      for (int i = 0; i < len; i++) {
        marshalers[i] = new AnyInt64FieldMarshaler(values.get(i));
      }
      return new ArrayValueMarshaler(marshalers);
    }

    static ArrayValueMarshaler createDouble(List<Double> values) {
      int len = values.size();
      Marshaler[] marshalers = new AnyDoubleFieldMarshaler[len];
      for (int i = 0; i < len; i++) {
        marshalers[i] = new AnyDoubleFieldMarshaler(values.get(i));
      }
      return new ArrayValueMarshaler(marshalers);
    }

    private final Marshaler[] values;

    private ArrayValueMarshaler(Marshaler[] values) {
      super(calculateSize(values));
      this.values = values;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(ArrayValue.VALUES, values);
    }

    private static int calculateSize(Marshaler[] values) {
      return MarshalerUtil.sizeRepeatedMessage(ArrayValue.VALUES, values);
    }
  }
}
