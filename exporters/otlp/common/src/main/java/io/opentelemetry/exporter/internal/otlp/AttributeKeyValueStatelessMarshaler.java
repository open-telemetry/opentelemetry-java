/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import java.io.IOException;
import java.util.List;

/**
 * A Marshaler of key value pairs. See {@link KeyValueMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributeKeyValueStatelessMarshaler
    implements StatelessMarshaler2<AttributeKey<?>, Object> {
  public static final AttributeKeyValueStatelessMarshaler INSTANCE =
      new AttributeKeyValueStatelessMarshaler();
  private static final byte[] EMPTY_BYTES = new byte[0];

  private AttributeKeyValueStatelessMarshaler() {}

  @Override
  public void writeTo(
      Serializer output, AttributeKey<?> attributeKey, Object value, MarshalerContext context)
      throws IOException {
    if (attributeKey.getKey().isEmpty()) {
      output.serializeString(KeyValue.KEY, EMPTY_BYTES);
    } else if (attributeKey instanceof InternalAttributeKeyImpl) {
      byte[] keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
      output.serializeString(KeyValue.KEY, keyUtf8);
    } else {
      output.serializeStringWithContext(KeyValue.KEY, attributeKey.getKey(), context);
    }
    output.serializeMessageWithContext(
        KeyValue.VALUE, attributeKey, value, ValueStatelessMarshaler.INSTANCE, context);
  }

  @Override
  public int getBinarySerializedSize(
      AttributeKey<?> attributeKey, Object value, MarshalerContext context) {
    int size = 0;
    if (!attributeKey.getKey().isEmpty()) {
      if (attributeKey instanceof InternalAttributeKeyImpl) {
        byte[] keyUtf8 = ((InternalAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
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
      implements StatelessMarshaler2<AttributeKey<?>, Object> {
    static final ValueStatelessMarshaler INSTANCE = new ValueStatelessMarshaler();

    @SuppressWarnings("unchecked")
    @Override
    public int getBinarySerializedSize(
        AttributeKey<?> attributeKey, Object value, MarshalerContext context) {
      AttributeType attributeType = attributeKey.getType();
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
              attributeType,
              (List<Object>) value,
              AttributeArrayAnyValueStatelessMarshaler.INSTANCE,
              context);
        case VALUE:
          return AnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
              (Value<?>) value, context);
      }
      // Error prone ensures the switch statement is complete, otherwise only can happen with
      // unaligned versions which are not supported.
      throw new IllegalArgumentException("Unsupported attribute type.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(
        Serializer output, AttributeKey<?> attributeKey, Object value, MarshalerContext context)
        throws IOException {
      AttributeType attributeType = attributeKey.getType();
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
              attributeType,
              (List<Object>) value,
              AttributeArrayAnyValueStatelessMarshaler.INSTANCE,
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
}
