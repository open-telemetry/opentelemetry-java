/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.KeyAnyValue;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.ArrayValue;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A Marshaler of key value pairs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class KeyValueMarshaler extends MarshalerWithSize {

  private static final byte[] EMPTY_BYTES = new byte[0];
  private static final KeyValueMarshaler[] EMPTY_REPEATED = new KeyValueMarshaler[0];

  private final byte[] keyUtf8;
  private final Marshaler value;

  private KeyValueMarshaler(byte[] keyUtf8, Marshaler value) {
    super(calculateSize(keyUtf8, value));
    this.keyUtf8 = keyUtf8;
    this.value = value;
  }

  /** Returns Marshaler for the given KeyAnyValue. */
  public static KeyValueMarshaler createForKeyAnyValue(KeyAnyValue keyAnyValue) {
    return new KeyValueMarshaler(
        keyAnyValue.getKey().getBytes(StandardCharsets.UTF_8),
        AnyValueMarshaler.create(keyAnyValue.getAnyValue()));
  }

  /** Returns Marshalers for the given Attributes. */
  @SuppressWarnings("AvoidObjectArrays")
  public static KeyValueMarshaler[] createForAttributes(Attributes attributes) {
    if (attributes.isEmpty()) {
      return EMPTY_REPEATED;
    }

    KeyValueMarshaler[] marshalers = new KeyValueMarshaler[attributes.size()];
    attributes.forEach(
        new BiConsumer<AttributeKey<?>, Object>() {
          int index = 0;

          @Override
          public void accept(AttributeKey<?> attributeKey, Object o) {
            marshalers[index++] = create(attributeKey, o);
          }
        });
    return marshalers;
  }

  @SuppressWarnings("unchecked")
  private static KeyValueMarshaler create(AttributeKey<?> attributeKey, Object value) {
    byte[] keyUtf8;
    if (attributeKey.getKey().isEmpty()) {
      keyUtf8 = EMPTY_BYTES;
    } else if (attributeKey instanceof InternalAttributeKeyImpl) {
      keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
    } else {
      keyUtf8 = attributeKey.getKey().getBytes(StandardCharsets.UTF_8);
    }
    switch (attributeKey.getType()) {
      case STRING:
        return new KeyValueMarshaler(keyUtf8, StringAnyValueMarshaler.create((String) value));
      case LONG:
        return new KeyValueMarshaler(keyUtf8, IntAnyValueMarshaler.create((long) value));
      case BOOLEAN:
        return new KeyValueMarshaler(keyUtf8, BoolAnyValueMarshaler.create((boolean) value));
      case DOUBLE:
        return new KeyValueMarshaler(keyUtf8, DoubleAnyValueMarshaler.create((double) value));
      case STRING_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createString((List<String>) value));
      case LONG_ARRAY:
        return new KeyValueMarshaler(keyUtf8, ArrayAnyValueMarshaler.createInt((List<Long>) value));
      case BOOLEAN_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createBool((List<Boolean>) value));
      case DOUBLE_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createDouble((List<Double>) value));
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(KeyValue.KEY, keyUtf8);
    output.serializeMessage(KeyValue.VALUE, value);
  }

  public static void writeTo(
      Serializer output,
      MarshalerContext context,
      ProtoFieldInfo protoFieldInfo,
      Attributes attributes)
      throws IOException {
    output.writeStartRepeated(protoFieldInfo);

    if (!attributes.isEmpty()) {
      AttributesWriter attributesWriter =
          context.getInstance(AttributesWriter.class, AttributesWriter::new);
      attributesWriter.init(protoFieldInfo, output, context);
      attributes.forEach(attributesWriter);
    }

    output.writeEndRepeated();
  }

  private static void writeTo(
      Serializer output, MarshalerContext context, AttributeKey<?> attributeKey, Object value)
      throws IOException {
    byte[] keyUtf8 = context.getByteArray();
    output.serializeString(KeyValue.KEY, keyUtf8);

    output.writeStartMessage(KeyValue.VALUE, context.getSize());
    writeAttributeValue(output, context, attributeKey, value);
    output.writeEndMessage();
  }

  @SuppressWarnings("unchecked")
  private static void writeAttributeValue(
      Serializer output, MarshalerContext context, AttributeKey<?> attributeKey, Object value)
      throws IOException {
    switch (attributeKey.getType()) {
      case STRING:
        if (context.marshalStringNoAllocation()) {
          output.writeString(AnyValue.STRING_VALUE, (String) value, context.getSize());
        } else {
          byte[] valueUtf8 = context.getByteArray();
          // Do not call serialize* method because we always have to write the message tag even if
          // the value is empty since it's a oneof.
          output.writeString(AnyValue.STRING_VALUE, valueUtf8);
        }
        return;
      case LONG:
        // Do not call serialize* method because we always have to write the message tag even if the
        // value is empty since it's a oneof.
        output.writeInt64(AnyValue.INT_VALUE, (long) value);
        return;
      case BOOLEAN:
        // Do not call serialize* method because we always have to write the message tag even if the
        // value is empty since it's a oneof.
        output.writeBool(AnyValue.BOOL_VALUE, (boolean) value);
        return;
      case DOUBLE:
        // Do not call serialize* method because we always have to write the message tag even if the
        // value is empty since it's a oneof.
        output.writeDouble(AnyValue.DOUBLE_VALUE, (double) value);
        return;
      case STRING_ARRAY:
        // output.serializeMessage(AnyValue.ARRAY_VALUE, value);
        writeToStringArray(context, output, (List<String>) value);
        return;
      case LONG_ARRAY:
        // output.serializeMessage(AnyValue.ARRAY_VALUE, value);
        writeToLongArray(context, output, (List<Long>) value);
        return;
      case BOOLEAN_ARRAY:
        writeToBooleanArray(context, output, (List<Boolean>) value);
        return;
      case DOUBLE_ARRAY:
        writeToDoubleArray(context, output, (List<Double>) value);
        return;
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private static void writeToStringArray(
      MarshalerContext context, Serializer output, List<String> values) throws IOException {
    output.writeStartMessage(AnyValue.ARRAY_VALUE, context.getSize());
    if (context.marshalStringNoAllocation()) {
      for (String value : values) {
        output.writeStartMessage(AnyValue.STRING_VALUE, context.getSize());
        output.writeString(AnyValue.STRING_VALUE, value, context.getSize());
        output.writeEndMessage();
      }
    } else {
      for (int i = 0; i < values.size(); i++) {
        output.writeStartMessage(AnyValue.STRING_VALUE, context.getSize());
        byte[] valueUtf8 = context.getByteArray();
        output.writeString(AnyValue.STRING_VALUE, valueUtf8);
        output.writeEndMessage();
      }
    }
    output.writeEndMessage();
  }

  private static void writeToLongArray(
      MarshalerContext context, Serializer output, List<Long> values) throws IOException {
    output.writeStartMessage(AnyValue.ARRAY_VALUE, context.getSize());
    for (Long value : values) {
      output.writeStartMessage(AnyValue.INT_VALUE, context.getSize());
      output.writeInt64(AnyValue.INT_VALUE, value);
      output.writeEndMessage();
    }
    output.writeEndMessage();
  }

  private static void writeToBooleanArray(
      MarshalerContext context, Serializer output, List<Boolean> values) throws IOException {
    output.writeStartMessage(AnyValue.ARRAY_VALUE, context.getSize());
    for (Boolean value : values) {
      output.writeStartMessage(AnyValue.BOOL_VALUE, context.getSize());
      output.writeBool(AnyValue.BOOL_VALUE, value);
      output.writeEndMessage();
    }
    output.writeEndMessage();
  }

  private static void writeToDoubleArray(
      MarshalerContext context, Serializer output, List<Double> values) throws IOException {
    output.writeStartMessage(AnyValue.ARRAY_VALUE, context.getSize());
    for (Double value : values) {
      output.writeStartMessage(AnyValue.DOUBLE_VALUE, context.getSize());
      output.writeDouble(AnyValue.DOUBLE_VALUE, value);
      output.writeEndMessage();
    }
    output.writeEndMessage();
  }

  private static int calculateSize(byte[] keyUtf8, Marshaler value) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, value);
    return size;
  }

  public static int calculateSize(
      ProtoFieldInfo field, MarshalerContext context, Attributes attributes) {
    if (attributes.isEmpty()) {
      return 0;
    }

    AttributesSizeCalculator attributesSizeCalculator =
        context.getInstance(AttributesSizeCalculator.class, AttributesSizeCalculator::new);
    attributesSizeCalculator.init(field, context);
    attributes.forEach(attributesSizeCalculator);

    return attributesSizeCalculator.size;
  }

  private static int calculateSize(
      MarshalerContext context, AttributeKey<?> attributeKey, Object value) {
    byte[] keyUtf8;
    if (attributeKey.getKey().isEmpty()) {
      keyUtf8 = EMPTY_BYTES;
    } else if (attributeKey instanceof InternalAttributeKeyImpl) {
      keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
    } else {
      keyUtf8 = attributeKey.getKey().getBytes(StandardCharsets.UTF_8);
    }
    context.addData(keyUtf8);

    int sizeIndex = context.addSize();
    int valueSizeIndex = context.addSize();

    int size = 0;
    size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
    int valueSize = calculateValueSize(context, attributeKey, value);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, valueSize);

    context.setSize(sizeIndex, size);
    context.setSize(valueSizeIndex, valueSize);

    return size;
  }

  @SuppressWarnings("unchecked")
  private static int calculateValueSize(
      MarshalerContext context, AttributeKey<?> attributeKey, Object value) {
    switch (attributeKey.getType()) {
      case STRING:
        return StringAnyValueMarshaler.calculateSize(context, (String) value);
      case LONG:
        return IntAnyValueMarshaler.calculateSize((long) value);
      case BOOLEAN:
        return BoolAnyValueMarshaler.calculateSize((boolean) value);
      case DOUBLE:
        return DoubleAnyValueMarshaler.calculateSize((double) value);
      case STRING_ARRAY:
        return MarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES,
            StringAnyValueMarshaler::calculateSize,
            (List<String>) value,
            context);
      case LONG_ARRAY:
        return MarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES, IntAnyValueMarshaler::calculateSize, (List<Long>) value);
      case BOOLEAN_ARRAY:
        return MarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES, BoolAnyValueMarshaler::calculateSize, (List<Boolean>) value);
      case DOUBLE_ARRAY:
        return MarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES, DoubleAnyValueMarshaler::calculateSize, (List<Double>) value);
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private static class AttributesWriter implements BiConsumer<AttributeKey<?>, Object> {
    @SuppressWarnings("NullAway")
    ProtoFieldInfo field;

    @SuppressWarnings("NullAway")
    Serializer output;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void init(ProtoFieldInfo field, Serializer output, MarshalerContext context) {
      this.field = field;
      this.output = output;
      this.context = context;
    }

    @Override
    public void accept(AttributeKey<?> attributeKey, Object value) {
      try {
        output.writeStartRepeatedElement(field, context.getSize());
        // output.writeStartMessage(field, context.getSize());
        writeTo(output, context, attributeKey, value);
        // output.writeEndMessage();
        output.writeEndRepeatedElement();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static class AttributesSizeCalculator implements BiConsumer<AttributeKey<?>, Object> {
    int size;
    int fieldTagSize;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void init(ProtoFieldInfo field, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.context = context;
    }

    @Override
    public void accept(AttributeKey<?> attributeKey, Object value) {
      int fieldSize = calculateSize(context, attributeKey, value);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }
}
