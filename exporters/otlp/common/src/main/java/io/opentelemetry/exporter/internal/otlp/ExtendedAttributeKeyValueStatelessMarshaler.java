/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributeType;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.incubator.internal.InternalExtendedAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValueList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

/**
 * A Marshaler of {@link ExtendedAttributes} key value pairs. See {@link KeyValueMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("deprecation")
public final class ExtendedAttributeKeyValueStatelessMarshaler
    implements StatelessMarshaler2<ExtendedAttributeKey<?>, Object> {
  private static final ExtendedAttributeKeyValueStatelessMarshaler INSTANCE =
      new ExtendedAttributeKeyValueStatelessMarshaler();
  private static final byte[] EMPTY_BYTES = new byte[0];

  private ExtendedAttributeKeyValueStatelessMarshaler() {}

  /**
   * Serializes the {@code attributes}. This method reads elements from context, use together with
   * {@link ExtendedAttributeKeyValueStatelessMarshaler#sizeExtendedAttributes(ProtoFieldInfo,
   * ExtendedAttributes, MarshalerContext)}.
   */
  public static void serializeExtendedAttributes(
      Serializer output,
      ProtoFieldInfo field,
      ExtendedAttributes attributes,
      MarshalerContext context)
      throws IOException {
    output.writeStartRepeated(field);

    if (!attributes.isEmpty()) {
      try {
        attributes.forEach(
            (extendedAttributeKey, value) -> {
              try {
                output.writeStartRepeatedElement(field, context.getSize());
                INSTANCE.writeTo(output, extendedAttributeKey, value, context);
                output.writeEndRepeatedElement();
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    }

    output.writeEndRepeated();
  }

  /**
   * Sizes the {@code attributes}. This method adds elements to context, use together with {@link
   * ExtendedAttributeKeyValueStatelessMarshaler#serializeExtendedAttributes(Serializer,
   * ProtoFieldInfo, ExtendedAttributes, MarshalerContext)}.
   */
  public static int sizeExtendedAttributes(
      ProtoFieldInfo field, ExtendedAttributes attributes, MarshalerContext context) {
    if (attributes.isEmpty()) {
      return 0;
    }

    int[] size = new int[] {0};

    attributes.forEach(
        (extendedAttributeKey, value) -> {
          int sizeIndex = context.addSize();
          int fieldSize = INSTANCE.getBinarySerializedSize(extendedAttributeKey, value, context);
          context.setSize(sizeIndex, fieldSize);
          size[0] +=
              field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
        });

    return size[0];
  }

  @Override
  public void writeTo(
      Serializer output,
      ExtendedAttributeKey<?> attributeKey,
      Object value,
      MarshalerContext context)
      throws IOException {
    if (attributeKey.getKey().isEmpty()) {
      output.serializeString(KeyValue.KEY, EMPTY_BYTES);
    } else if (attributeKey instanceof InternalExtendedAttributeKeyImpl) {
      byte[] keyUtf8 = ((InternalExtendedAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
      output.serializeString(KeyValue.KEY, keyUtf8);
    } else {
      output.serializeStringWithContext(KeyValue.KEY, attributeKey.getKey(), context);
    }
    output.serializeMessageWithContext(
        KeyValue.VALUE, attributeKey, value, ValueStatelessMarshaler.INSTANCE, context);
  }

  @Override
  public int getBinarySerializedSize(
      ExtendedAttributeKey<?> attributeKey, Object value, MarshalerContext context) {
    int size = 0;
    if (!attributeKey.getKey().isEmpty()) {
      if (attributeKey instanceof InternalExtendedAttributeKeyImpl) {
        byte[] keyUtf8 = ((InternalExtendedAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
        size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
      } else {
        return StatelessMarshalerUtil.sizeStringWithContext(
            KeyValue.KEY, attributeKey.getKey(), context);
      }
    }
    size +=
        StatelessMarshalerUtil.sizeMessageWithContext(
            KeyValue.VALUE, attributeKey, value, ValueStatelessMarshaler.INSTANCE, context);

    return size;
  }

  private static class ValueStatelessMarshaler
      implements StatelessMarshaler2<ExtendedAttributeKey<?>, Object> {
    static final ValueStatelessMarshaler INSTANCE = new ValueStatelessMarshaler();

    // Supporting deprecated EXTENDED_ATTRIBUTES type until removed
    @SuppressWarnings("unchecked")
    @Override
    public int getBinarySerializedSize(
        ExtendedAttributeKey<?> attributeKey, Object value, MarshalerContext context) {
      ExtendedAttributeType attributeType = attributeKey.getType();
      switch (attributeType) {
        case STRING:
          return StringAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (String) value, context);
        case LONG:
          return IntAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (Long) value, context);
        case BOOLEAN:
          return BoolAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (Boolean) value, context);
        case DOUBLE:
          return DoubleAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (Double) value, context);
        case STRING_ARRAY:
        case LONG_ARRAY:
        case BOOLEAN_ARRAY:
        case DOUBLE_ARRAY:
          return StatelessMarshalerUtil.sizeMessageWithContext(
              AnyValue.ARRAY_VALUE,
              Objects.requireNonNull(attributeKey.asAttributeKey()).getType(),
              (List<Object>) value,
              AttributeArrayAnyValueStatelessMarshaler.INSTANCE,
              context);
        case EXTENDED_ATTRIBUTES:
          return StatelessMarshalerUtil.sizeMessageWithContext(
              AnyValue.KVLIST_VALUE,
              (ExtendedAttributes) value,
              ExtendedAttributesKeyValueListStatelessMarshaler.INSTANCE,
              context);
        case VALUE:
          return AnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (Value<?>) value, context);
      }
      // Error prone ensures the switch statement is complete, otherwise only can happen with
      // unaligned versions which are not supported.
      throw new IllegalArgumentException("Unsupported attribute type.");
    }

    // Supporting deprecated EXTENDED_ATTRIBUTES type until removed
    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(
        Serializer output,
        ExtendedAttributeKey<?> attributeKey,
        Object value,
        MarshalerContext context)
        throws IOException {
      ExtendedAttributeType attributeType = attributeKey.getType();
      switch (attributeType) {
        case STRING:
          StringAnyValueStatelessMarshaler.INSTANCE.writeTo(output, (String) value, context);
          return;
        case LONG:
          IntAnyValueStatelessMarshaler.INSTANCE.writeTo(output, (Long) value, context);
          return;
        case BOOLEAN:
          BoolAnyValueStatelessMarshaler.INSTANCE.writeTo(output, (Boolean) value, context);
          return;
        case DOUBLE:
          DoubleAnyValueStatelessMarshaler.INSTANCE.writeTo(output, (Double) value, context);
          return;
        case STRING_ARRAY:
        case LONG_ARRAY:
        case BOOLEAN_ARRAY:
        case DOUBLE_ARRAY:
          output.serializeMessageWithContext(
              AnyValue.ARRAY_VALUE,
              Objects.requireNonNull(attributeKey.asAttributeKey()).getType(),
              (List<Object>) value,
              AttributeArrayAnyValueStatelessMarshaler.INSTANCE,
              context);
          return;
        case EXTENDED_ATTRIBUTES:
          output.serializeMessageWithContext(
              AnyValue.KVLIST_VALUE,
              (ExtendedAttributes) value,
              ExtendedAttributesKeyValueListStatelessMarshaler.INSTANCE,
              context);
          return;
        case VALUE:
          AnyValueStatelessMarshaler.INSTANCE.writeTo(output, (Value<?>) value, context);
          return;
      }
      // Error prone ensures the switch statement is complete, otherwise only can happen with
      // unaligned versions which are not supported.
      throw new IllegalArgumentException("Unsupported attribute type.");
    }
  }

  private static class ExtendedAttributesKeyValueListStatelessMarshaler
      implements StatelessMarshaler<ExtendedAttributes> {
    private static final ExtendedAttributesKeyValueListStatelessMarshaler INSTANCE =
        new ExtendedAttributesKeyValueListStatelessMarshaler();

    private ExtendedAttributesKeyValueListStatelessMarshaler() {}

    @Override
    public void writeTo(Serializer output, ExtendedAttributes value, MarshalerContext context)
        throws IOException {
      serializeExtendedAttributes(output, KeyValueList.VALUES, value, context);
    }

    @Override
    public int getBinarySerializedSize(ExtendedAttributes value, MarshalerContext context) {
      return sizeExtendedAttributes(KeyValueList.VALUES, value, context);
    }
  }
}
