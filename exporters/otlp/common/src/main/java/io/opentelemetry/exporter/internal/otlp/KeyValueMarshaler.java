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
      attributesWriter.initialize(protoFieldInfo, output, context);
      attributes.forEach(attributesWriter);
    }

    output.writeEndRepeated();
  }

  private static void writeTo(
      Serializer output, MarshalerContext context, AttributeKey<?> attributeKey, Object value)
      throws IOException {
    if (attributeKey.getKey().isEmpty()) {
      output.serializeString(KeyValue.KEY, EMPTY_BYTES);
    } else if (attributeKey instanceof InternalAttributeKeyImpl) {
      byte[] keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
      output.serializeString(KeyValue.KEY, keyUtf8);
    } else if (context.marshalStringNoAllocation()) {
      output.writeString(KeyValue.KEY, attributeKey.getKey(), context.getSize());
    } else {
      output.serializeString(KeyValue.KEY, context.getByteArray());
    }

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
        StringAnyValueMarshaler.writeTo(output, (String) value, context);
        return;
      case LONG:
        IntAnyValueMarshaler.writeTo(output, (long) value);
        return;
      case BOOLEAN:
        BoolAnyValueMarshaler.writeTo(output, (boolean) value);
        return;
      case DOUBLE:
        DoubleAnyValueMarshaler.writeTo(output, (double) value);
        return;
      case STRING_ARRAY:
        ArrayAnyValueMarshaler.writeTo(
            output, (List<String>) value, StringAnyValueMarshaler::writeTo, context);
        return;
      case LONG_ARRAY:
        ArrayAnyValueMarshaler.writeTo(
            output, (List<Long>) value, IntAnyValueMarshaler::writeTo, context);
        return;
      case BOOLEAN_ARRAY:
        ArrayAnyValueMarshaler.writeTo(
            output, (List<Boolean>) value, BoolAnyValueMarshaler::writeTo, context);
        return;
      case DOUBLE_ARRAY:
        ArrayAnyValueMarshaler.writeTo(
            output, (List<Double>) value, DoubleAnyValueMarshaler::writeTo, context);
        return;
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private static int calculateSize(byte[] keyUtf8, Marshaler value) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, value);
    return size;
  }

  public static int calculateSize(
      ProtoFieldInfo field, Attributes attributes, MarshalerContext context) {
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
      AttributeKey<?> attributeKey, Object value, MarshalerContext context) {
    int sizeIndex = context.addSize();
    int size = 0;
    if (!attributeKey.getKey().isEmpty()) {
      if (attributeKey instanceof InternalAttributeKeyImpl) {
        byte[] keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
        size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
      } else if (context.marshalStringNoAllocation()) {
        int utf8Size = MarshalerUtil.getUtf8Size(attributeKey.getKey());
        context.addSize(utf8Size);
        size += MarshalerUtil.sizeBytes(KeyValue.KEY, utf8Size);
      } else {
        byte[] keyUtf8 = attributeKey.getKey().getBytes(StandardCharsets.UTF_8);
        context.addData(keyUtf8);
        size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
      }
    }
    int valueSizeIndex = context.addSize();
    int valueSize = calculateValueSize(attributeKey, value, context);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, valueSize);

    context.setSize(sizeIndex, size);
    context.setSize(valueSizeIndex, valueSize);

    return size;
  }

  @SuppressWarnings("unchecked")
  private static int calculateValueSize(
      AttributeKey<?> attributeKey, Object value, MarshalerContext context) {
    switch (attributeKey.getType()) {
      case STRING:
        return StringAnyValueMarshaler.calculateSize((String) value, context);
      case LONG:
        return IntAnyValueMarshaler.calculateSize((long) value);
      case BOOLEAN:
        return BoolAnyValueMarshaler.calculateSize((boolean) value);
      case DOUBLE:
        return DoubleAnyValueMarshaler.calculateSize((double) value);
      case STRING_ARRAY:
        return ArrayAnyValueMarshaler.calculateSize(
            (List<String>) value, StringAnyValueMarshaler::calculateSize, context);
      case LONG_ARRAY:
        return ArrayAnyValueMarshaler.calculateSize(
            (List<Long>) value, IntAnyValueMarshaler::calculateSize, context);
      case BOOLEAN_ARRAY:
        return ArrayAnyValueMarshaler.calculateSize(
            (List<Boolean>) value, BoolAnyValueMarshaler::calculateSize, context);
      case DOUBLE_ARRAY:
        return ArrayAnyValueMarshaler.calculateSize(
            (List<Double>) value, DoubleAnyValueMarshaler::calculateSize, context);
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

    void initialize(ProtoFieldInfo field, Serializer output, MarshalerContext context) {
      this.field = field;
      this.output = output;
      this.context = context;
    }

    @Override
    public void accept(AttributeKey<?> attributeKey, Object value) {
      try {
        output.writeStartRepeatedElement(field, context.getSize());
        writeTo(output, context, attributeKey, value);
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
      int fieldSize = calculateSize(attributeKey, value, context);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }
}
