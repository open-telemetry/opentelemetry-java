/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

abstract class AttributeMarshaler extends MarshalerWithSize {
  private static final AttributeMarshaler[] EMPTY_REPEATED = new AttributeMarshaler[0];
  private final byte[] key;
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
    byte[] key = MarshalerUtil.toBytes(attributeKey.getKey());
    if (value == null) {
      return new KeyValueNullMarshaler(key);
    }
    switch (attributeKey.getType()) {
      case STRING:
        return new KeyValueStringMarshaler(key, MarshalerUtil.toBytes((String) value));
      case LONG:
        return new KeyValueLongMarshaler(key, (Long) value);
      case BOOLEAN:
        return new KeyValueBooleanMarshaler(key, (Boolean) value);
      case DOUBLE:
        return new KeyValueDoubleMarshaler(key, (Double) value);
      case STRING_ARRAY:
        return new KeyValueArrayStringMarshaler(key, (List<String>) value);
      case LONG_ARRAY:
        return new KeyValueArrayLongMarshaler(key, (List<Long>) value);
      case BOOLEAN_ARRAY:
        return new KeyValueArrayBooleanMarshaler(key, (List<Boolean>) value);
      case DOUBLE_ARRAY:
        return new KeyValueArrayDoubleMarshaler(key, (List<Double>) value);
    }
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private AttributeMarshaler(byte[] key, int valueSize) {
    super(calculateSize(key, valueSize));
    this.key = key;
    this.valueSize = valueSize;
  }

  @Override
  public final void writeTo(CodedOutputStream output) throws IOException {
    MarshalerUtil.marshalBytes(KeyValue.KEY_FIELD_NUMBER, key, output);
    if (valueSize > 0) {
      output.writeTag(KeyValue.VALUE_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      output.writeUInt32NoTag(valueSize);
      writeValueTo(output);
    }
  }

  abstract void writeValueTo(CodedOutputStream output) throws IOException;

  private static int calculateSize(byte[] key, int valueSize) {
    return MarshalerUtil.sizeBytes(KeyValue.KEY_FIELD_NUMBER, key)
        + CodedOutputStream.computeTagSize(KeyValue.VALUE_FIELD_NUMBER)
        + CodedOutputStream.computeUInt32SizeNoTag(valueSize)
        + valueSize;
  }

  private static final class KeyValueNullMarshaler extends AttributeMarshaler {
    private KeyValueNullMarshaler(byte[] key) {
      super(key, 0);
    }

    @Override
    void writeValueTo(CodedOutputStream output) {}
  }

  private static final class KeyValueStringMarshaler extends AttributeMarshaler {
    private final byte[] value;

    private KeyValueStringMarshaler(byte[] key, byte[] value) {
      super(key, CodedOutputStream.computeByteArraySize(AnyValue.STRING_VALUE_FIELD_NUMBER, value));
      this.value = value;
    }

    @Override
    public void writeValueTo(CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeByteArray(AnyValue.STRING_VALUE_FIELD_NUMBER, value);
    }
  }

  private static final class KeyValueLongMarshaler extends AttributeMarshaler {
    private final long value;

    private KeyValueLongMarshaler(byte[] key, long value) {
      super(key, CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE_FIELD_NUMBER, value));
      this.value = value;
    }

    @Override
    public void writeValueTo(CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeInt64(AnyValue.INT_VALUE_FIELD_NUMBER, value);
    }
  }

  private static final class KeyValueBooleanMarshaler extends AttributeMarshaler {
    private final boolean value;

    private KeyValueBooleanMarshaler(byte[] key, boolean value) {
      super(key, CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE_FIELD_NUMBER, value));
      this.value = value;
    }

    @Override
    public void writeValueTo(CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeBool(AnyValue.BOOL_VALUE_FIELD_NUMBER, value);
    }
  }

  private static final class KeyValueDoubleMarshaler extends AttributeMarshaler {
    private final double value;

    private KeyValueDoubleMarshaler(byte[] key, double value) {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      super(key, CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE_FIELD_NUMBER, value));
      this.value = value;
    }

    @Override
    public void writeValueTo(CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeDouble(AnyValue.DOUBLE_VALUE_FIELD_NUMBER, value);
    }
  }

  private abstract static class KeyValueArrayMarshaler<T> extends AttributeMarshaler {
    private final List<T> values;
    private final int valuesSize;

    private KeyValueArrayMarshaler(byte[] key, List<T> values, int valuesSize) {
      super(key, calculateWrapperSize(valuesSize) + valuesSize);
      this.values = values;
      this.valuesSize = valuesSize;
    }

    @Override
    public final void writeValueTo(CodedOutputStream output) throws IOException {
      output.writeTag(AnyValue.ARRAY_VALUE_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      output.writeUInt32NoTag(valuesSize);
      for (T value : values) {
        output.writeTag(ArrayValue.VALUES_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        output.writeUInt32NoTag(getArrayElementSerializedSize(value));
        writeArrayElementTo(value, output);
      }
    }

    abstract void writeArrayElementTo(T value, CodedOutputStream output) throws IOException;

    abstract int getArrayElementSerializedSize(T value);

    private static int calculateWrapperSize(int valuesSize) {
      return CodedOutputStream.computeTagSize(AnyValue.ARRAY_VALUE_FIELD_NUMBER)
          + CodedOutputStream.computeUInt32SizeNoTag(valuesSize);
    }
  }

  private static final class KeyValueArrayStringMarshaler extends KeyValueArrayMarshaler<String> {
    private KeyValueArrayStringMarshaler(byte[] key, List<String> values) {
      super(key, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(String value, CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeString(AnyValue.STRING_VALUE_FIELD_NUMBER, value);
    }

    @Override
    int getArrayElementSerializedSize(String value) {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      return CodedOutputStream.computeStringSize(AnyValue.STRING_VALUE_FIELD_NUMBER, value);
    }

    static int calculateValuesSize(List<String> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES_FIELD_NUMBER);
      for (String value : values) {
        // Do not call MarshalUtil because we always have to write the message tag even if the value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeStringSize(AnyValue.STRING_VALUE_FIELD_NUMBER, value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayLongMarshaler extends KeyValueArrayMarshaler<Long> {
    private KeyValueArrayLongMarshaler(byte[] key, List<Long> values) {
      super(key, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Long value, CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeInt64(AnyValue.INT_VALUE_FIELD_NUMBER, value);
    }

    @Override
    int getArrayElementSerializedSize(Long value) {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      return CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE_FIELD_NUMBER, value);
    }

    static int calculateValuesSize(List<Long> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES_FIELD_NUMBER);
      for (Long value : values) {
        // Do not call MarshalUtil because we always have to write the message tag even if the value
        // is empty.
        int fieldSize = CodedOutputStream.computeInt64Size(AnyValue.INT_VALUE_FIELD_NUMBER, value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayBooleanMarshaler extends KeyValueArrayMarshaler<Boolean> {
    private KeyValueArrayBooleanMarshaler(byte[] key, List<Boolean> values) {
      super(key, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Boolean value, CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeBool(AnyValue.BOOL_VALUE_FIELD_NUMBER, value);
    }

    @Override
    int getArrayElementSerializedSize(Boolean value) {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      return CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE_FIELD_NUMBER, value);
    }

    static int calculateValuesSize(List<Boolean> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES_FIELD_NUMBER);
      for (Boolean value : values) {
        // Do not call MarshalUtil because we always have to write the message tag even if the value
        // is empty.
        int fieldSize = CodedOutputStream.computeBoolSize(AnyValue.BOOL_VALUE_FIELD_NUMBER, value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }

  private static final class KeyValueArrayDoubleMarshaler extends KeyValueArrayMarshaler<Double> {
    private KeyValueArrayDoubleMarshaler(byte[] key, List<Double> values) {
      super(key, values, calculateValuesSize(values));
    }

    @Override
    void writeArrayElementTo(Double value, CodedOutputStream output) throws IOException {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      output.writeDouble(AnyValue.DOUBLE_VALUE_FIELD_NUMBER, value);
    }

    @Override
    int getArrayElementSerializedSize(Double value) {
      // Do not call MarshalUtil because we always have to write the message tag even if the value
      // is empty.
      return CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE_FIELD_NUMBER, value);
    }

    static int calculateValuesSize(List<Double> values) {
      int size = 0;
      int fieldTagSize = CodedOutputStream.computeTagSize(ArrayValue.VALUES_FIELD_NUMBER);
      for (Double value : values) {
        // Do not call MarshalUtil because we always have to write the message tag even if the value
        // is empty.
        int fieldSize =
            CodedOutputStream.computeDoubleSize(AnyValue.DOUBLE_VALUE_FIELD_NUMBER, value);
        size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
      }
      return size;
    }
  }
}
