/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.ArrayValue;
import java.io.IOException;
import java.util.List;

/** See {@link ArrayAnyValueMarshaler}. */
// TODO: add support for List<io.opentelemetry.api.incubator.logs.AnyValue<?>>
final class ArrayAnyValueStatelessMarshaler<T>
    implements StatelessMarshaler2<AttributeType, List<T>> {
  static final ArrayAnyValueStatelessMarshaler<Object> INSTANCE =
      new ArrayAnyValueStatelessMarshaler<>();

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(Serializer output, AttributeType type, List<T> list, MarshalerContext context)
      throws IOException {
    switch (type) {
      case STRING_ARRAY:
        output.serializeRepeatedMessage(
            ArrayValue.VALUES,
            (List<String>) list,
            StringAnyValueStatelessMarshaler.INSTANCE,
            context);
        return;
      case LONG_ARRAY:
        output.serializeRepeatedMessage(
            ArrayValue.VALUES, (List<Long>) list, IntAnyValueStatelessMarshaler.INSTANCE, context);
        return;
      case BOOLEAN_ARRAY:
        output.serializeRepeatedMessage(
            ArrayValue.VALUES,
            (List<Boolean>) list,
            BoolAnyValueStatelessMarshaler.INSTANCE,
            context);
        return;
      case DOUBLE_ARRAY:
        output.serializeRepeatedMessage(
            ArrayValue.VALUES,
            (List<Double>) list,
            DoubleAnyValueStatelessMarshaler.INSTANCE,
            context);
        return;
      default:
        throw new IllegalArgumentException("Unsupported attribute type.");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public int getBinarySerializedSize(AttributeType type, List<T> list, MarshalerContext context) {
    switch (type) {
      case STRING_ARRAY:
        return StatelessMarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES,
            (List<String>) list,
            StringAnyValueStatelessMarshaler.INSTANCE,
            context);
      case LONG_ARRAY:
        return StatelessMarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES, (List<Long>) list, IntAnyValueStatelessMarshaler.INSTANCE, context);
      case BOOLEAN_ARRAY:
        return StatelessMarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES,
            (List<Boolean>) list,
            BoolAnyValueStatelessMarshaler.INSTANCE,
            context);
      case DOUBLE_ARRAY:
        return StatelessMarshalerUtil.sizeRepeatedMessage(
            ArrayValue.VALUES,
            (List<Double>) list,
            DoubleAnyValueStatelessMarshaler.INSTANCE,
            context);
      default:
        throw new IllegalArgumentException("Unsupported attribute type.");
    }
  }
}
