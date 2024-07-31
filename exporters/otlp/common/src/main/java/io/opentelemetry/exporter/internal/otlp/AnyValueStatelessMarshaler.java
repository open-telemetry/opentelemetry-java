/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A Marshaler of key value pairs. See {@link AnyValueMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AnyValueStatelessMarshaler implements StatelessMarshaler<Value<?>> {

  public static final AnyValueStatelessMarshaler INSTANCE = new AnyValueStatelessMarshaler();

  private AnyValueStatelessMarshaler() {}

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(Serializer output, Value<?> value, MarshalerContext context)
      throws IOException {
    switch (value.getType()) {
      case STRING:
        StringAnyValueStatelessMarshaler.INSTANCE.writeTo(
            output, (String) value.getValue(), context);
        return;
      case BOOLEAN:
        BoolAnyValueStatelessMarshaler.INSTANCE.writeTo(
            output, (Boolean) value.getValue(), context);
        return;
      case LONG:
        IntAnyValueStatelessMarshaler.INSTANCE.writeTo(output, (Long) value.getValue(), context);
        return;
      case DOUBLE:
        DoubleAnyValueStatelessMarshaler.INSTANCE.writeTo(
            output, (Double) value.getValue(), context);
        return;
      case ARRAY:
        output.serializeMessageWithContext(
            io.opentelemetry.proto.common.v1.internal.AnyValue.ARRAY_VALUE,
            (List<Value<?>>) value.getValue(),
            ArrayAnyValueStatelessMarshaler.INSTANCE,
            context);
        return;
      case KEY_VALUE_LIST:
        output.serializeMessageWithContext(
            io.opentelemetry.proto.common.v1.internal.AnyValue.KVLIST_VALUE,
            (List<KeyValue>) value.getValue(),
            KeyValueListAnyValueStatelessMarshaler.INSTANCE,
            context);
        return;
      case BYTES:
        BytesAnyValueStatelessMarshaler.INSTANCE.writeTo(
            output, (ByteBuffer) value.getValue(), context);
        return;
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported value type.");
  }

  @SuppressWarnings("unchecked")
  @Override
  public int getBinarySerializedSize(Value<?> value, MarshalerContext context) {
    switch (value.getType()) {
      case STRING:
        return StringAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
            (String) value.getValue(), context);
      case BOOLEAN:
        return BoolAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
            (Boolean) value.getValue(), context);
      case LONG:
        return IntAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
            (Long) value.getValue(), context);
      case DOUBLE:
        return DoubleAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
            (Double) value.getValue(), context);
      case ARRAY:
        return StatelessMarshalerUtil.sizeMessageWithContext(
            io.opentelemetry.proto.common.v1.internal.AnyValue.ARRAY_VALUE,
            (List<Value<?>>) value.getValue(),
            ArrayAnyValueStatelessMarshaler.INSTANCE,
            context);
      case KEY_VALUE_LIST:
        return StatelessMarshalerUtil.sizeMessageWithContext(
            io.opentelemetry.proto.common.v1.internal.AnyValue.KVLIST_VALUE,
            (List<KeyValue>) value.getValue(),
            KeyValueListAnyValueStatelessMarshaler.INSTANCE,
            context);
      case BYTES:
        return BytesAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(
            (ByteBuffer) value.getValue(), context);
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported value type.");
  }
}
