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

abstract class AttributeMarshaler extends MarshalerWithSize {
  private static final AttributeMarshaler[] EMPTY_REPEATED = new AttributeMarshaler[0];
  private final byte[] keyUtf8;
  private final int valueSize;

  static AttributeMarshaler[] createRepeated(Attributes attributes) {
    if (attributes.isEmpty()) {
      return EMPTY_REPEATED;
    }

    AttributeMarshaler[] attributeMarshalers = new AttributeMarshaler[attributes.size()];
    attributes.forEach(
        new BiConsumer<AttributeKey<?>, Object>() {
          int index = 0;

          @Override
          public void accept(AttributeKey<?> attributeKey, Object o) {
            attributeMarshalers[index++] = AttributeMarshaler.create(attributeKey, o);
          }
        });
    return attributeMarshalers;
  }

  @SuppressWarnings("unchecked")
  static AttributeMarshaler create(AttributeKey<?> attributeKey, Object value) {
    byte[] keyUtf8Utf8 = MarshalerUtil.toBytes(attributeKey.getKey());
    if (value == null) {
      return new KeyValueNullMarshaler(keyUtf8Utf8);
    }
    switch (attributeKey.getType()) {
      case STRING:
        return new KeyValueStringMarshaler(keyUtf8Utf8, MarshalerUtil.toBytes((String) value));
      case LONG:
        return new KeyValueLongMarshaler(keyUtf8Utf8, (Long) value);
      case BOOLEAN:
        return new KeyValueBooleanMarshaler(keyUtf8Utf8, (Boolean) value);
      case DOUBLE:
        return new KeyValueDoubleMarshaler(keyUtf8Utf8, (Double) value);
      case STRING_ARRAY:
        return new KeyValueArrayStringMarshaler(keyUtf8Utf8, (List<String>) value);
      case LONG_ARRAY:
        return new KeyValueArrayLongMarshaler(keyUtf8Utf8, (List<Long>) value);
      case BOOLEAN_ARRAY:
        return new KeyValueArrayBooleanMarshaler(keyUtf8Utf8, (List<Boolean>) value);
      case DOUBLE_ARRAY:
        return new KeyValueArrayDoubleMarshaler(keyUtf8Utf8, (List<Double>) value);
    }
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private AttributeMarshaler(byte[] keyUtf8, int valueSize) {
    super(calculateSize(keyUtf8, valueSize));
    this.keyUtf8 = keyUtf8;
    this.valueSize = valueSize;
  }

  @Override
  public final void writeTo(Serializer output) throws IOException {
    output.serializeString(KeyValue.KEY, keyUtf8);
    if (valueSize > 0) {
      // TODO(anuraaga): Replace this hack with directly serializing Value within Serializer. The
      // proto and JSON representations of Value differ too much to use Marshaler.
      CodedOutputStream cos = ((ProtoSerializer) output).getCodedOutputStream();
      cos.writeTag(KeyValue.VALUE.getFieldNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(valueSize);
      writeValueTo(output);
    }
  }

  abstract void writeValueTo(Serializer output) throws IOException;

  private static int calculateSize(byte[] keyUtf8, int valueSize) {
    return MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8)
        + CodedOutputStream.computeTagSize(KeyValue.VALUE.getFieldNumber())
        + CodedOutputStream.computeUInt32SizeNoTag(valueSize)
        + valueSize;
  }

  private static final class KeyValueNullMarshaler extends AttributeMarshaler {
    private KeyValueNullMarshaler(byte[] keyUtf8) {
      super(keyUtf8, 0);
    }

    @Override
    void writeValueTo(Serializer output) {}
  }

  private static final class KeyValueStringMarshaler extends AttributeMarshaler {
    private final byte[] value;

    private KeyValueStringMarshaler(byte[] keyUtf8, byte[] value) {
      super(
          keyUtf8,
          CodedOutputStream.computeByteArraySize(AnyValue.STRING_VALUE.getFieldNumber(), value));
      this.value = value;
    }

    @Override
    public void writeValueTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeString(AnyValue.STRING_VALUE, value);
    }
  }

  private static final class KeyValueLongMarshaler extends AttributeMarshaler {
    private final long value;

    private KeyValueLongMarshaler(byte[] keyUtf8, long value) {
      super(
          keyUtf8, CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE.getFieldNumber(), value));
      this.value = value;
    }

    @Override
    public void writeValueTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeInt64(AnyValue.INT_VALUE, value);
    }
  }

  private static final class KeyValueBooleanMarshaler extends AttributeMarshaler {
    private final boolean value;

    private KeyValueBooleanMarshaler(byte[] keyUtf8, boolean value) {
      super(
          keyUtf8, CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE.getFieldNumber(), value));
      this.value = value;
    }

    @Override
    public void writeValueTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeBool(AnyValue.BOOL_VALUE, value);
    }
  }

  private static final class KeyValueDoubleMarshaler extends AttributeMarshaler {
    private final double value;

    private KeyValueDoubleMarshaler(byte[] keyUtf8, double value) {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      super(
          keyUtf8,
          CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE.getFieldNumber(), value));
      this.value = value;
    }

    @Override
    public void writeValueTo(Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeDouble(AnyValue.DOUBLE_VALUE, value);
    }
  }

  private abstract static class KeyValueArrayMarshaler<T> extends AttributeMarshaler {
    private final List<T> values;
    private final int valuesSize;

    private KeyValueArrayMarshaler(byte[] keyUtf8, List<T> values, int valuesSize) {
      super(keyUtf8, calculateWrapperSize(valuesSize) + valuesSize);
      this.values = values;
      this.valuesSize = valuesSize;
    }

    @Override
    public final void writeValueTo(Serializer output) throws IOException {
      // TODO(anuraaga): Replace this hack with directly serializing Value within Serializer. The
      // proto and JSON representations of Value differ too much to use Marshaler.
      CodedOutputStream cos = ((ProtoSerializer) output).getCodedOutputStream();
      cos.writeTag(AnyValue.ARRAY_VALUE.getFieldNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(valuesSize);
      for (T value : values) {
        cos.writeTag(ArrayValue.VALUES.getFieldNumber(), WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(getArrayElementSerializedSize(value));
        writeArrayElementTo(value, output);
      }
    }

    abstract void writeArrayElementTo(T value, Serializer output) throws IOException;

    abstract int getArrayElementSerializedSize(T value);

    private static int calculateWrapperSize(int valuesSize) {
      return CodedOutputStream.computeTagSize(AnyValue.ARRAY_VALUE.getFieldNumber())
          + CodedOutputStream.computeUInt32SizeNoTag(valuesSize);
    }
  }

  private static final class KeyValueArrayStringMarshaler extends KeyValueArrayMarshaler<String> {
    private KeyValueArrayStringMarshaler(byte[] keyUtf8, List<String> values) {
      super(keyUtf8, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(String value, Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeString(AnyValue.STRING_VALUE, value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    int getArrayElementSerializedSize(String value) {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      return CodedOutputStream.computeStringSize(AnyValue.STRING_VALUE.getFieldNumber(), value);
    }

    static int calculateValuesSize(List<String> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES.getFieldNumber());
      for (String value : values) {
        // Do not call serialize* method because we always have to write the message tag even if the
        // value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeStringSize(AnyValue.STRING_VALUE.getFieldNumber(), value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayLongMarshaler extends KeyValueArrayMarshaler<Long> {
    private KeyValueArrayLongMarshaler(byte[] keyUtf8, List<Long> values) {
      super(keyUtf8, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Long value, Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeInt64(AnyValue.INT_VALUE, value);
    }

    @Override
    int getArrayElementSerializedSize(Long value) {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      return CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE.getFieldNumber(), value);
    }

    static int calculateValuesSize(List<Long> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES.getFieldNumber());
      for (Long value : values) {
        // Do not call serialize* method because we always have to write the message tag even if the
        // value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE.getFieldNumber(), value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayBooleanMarshaler extends KeyValueArrayMarshaler<Boolean> {
    private KeyValueArrayBooleanMarshaler(byte[] keyUtf8, List<Boolean> values) {
      super(keyUtf8, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Boolean value, Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeBool(AnyValue.BOOL_VALUE, value);
    }

    @Override
    int getArrayElementSerializedSize(Boolean value) {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      return CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE.getFieldNumber(), value);
    }

    static int calculateValuesSize(List<Boolean> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES.getFieldNumber());
      for (Boolean value : values) {
        // Do not call serialize* method because we always have to write the message tag even if the
        // value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE.getFieldNumber(), value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayDoubleMarshaler extends KeyValueArrayMarshaler<Double> {
    private KeyValueArrayDoubleMarshaler(byte[] keyUtf8, List<Double> values) {
      super(keyUtf8, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Double value, Serializer output) throws IOException {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      output.writeDouble(AnyValue.DOUBLE_VALUE, value);
    }

    @Override
    int getArrayElementSerializedSize(Double value) {
      // Do not call serialize* method because we always have to write the message tag even if the
      // value
      // is empty.
      return CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE.getFieldNumber(), value);
    }

    static int calculateValuesSize(List<Double> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES.getFieldNumber());
      for (Double value : values) {
        // Do not call serialize* method because we always have to write the message tag even if the
        // value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE.getFieldNumber(), value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }
}
