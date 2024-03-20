/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.MarshallingObjectsPool;
import io.opentelemetry.exporter.internal.marshal.MessageSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import io.opentelemetry.extension.incubator.logs.KeyAnyValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Utility methods for obtaining AnyValue marshaler.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AnyValueMarshaler {

  private AnyValueMarshaler() {}

  @SuppressWarnings("unchecked")
  public static MarshalerWithSize create(AnyValue<?> anyValue) {
    switch (anyValue.getType()) {
      case STRING:
        return StringAnyValueMarshaler.create((String) anyValue.getValue());
      case BOOLEAN:
        return BoolAnyValueMarshaler.create((boolean) anyValue.getValue());
      case LONG:
        return IntAnyValueMarshaler.create((long) anyValue.getValue());
      case DOUBLE:
        return DoubleAnyValueMarshaler.create((double) anyValue.getValue());
      case ARRAY:
        return ArrayAnyValueMarshaler.createAnyValue((List<AnyValue<?>>) anyValue.getValue());
      case KEY_VALUE_LIST:
        return KeyValueListAnyValueMarshaler.create((List<KeyAnyValue>) anyValue.getValue());
      case BYTES:
        return BytesAnyValueMarshaler.create((ByteBuffer) anyValue.getValue());
    }
    throw new IllegalArgumentException("Unsupported AnyValue type: " + anyValue.getType());
  }

  @SuppressWarnings("unchecked")
  public static MessageSize messageSize(AnyValue<?> anyValue, MarshallingObjectsPool pool) {
    switch (anyValue.getType()) {
      case STRING:
        return StringAnyValueMarshaler.messageSize((String) anyValue.getValue(), pool);
      case BOOLEAN:
        return BoolAnyValueMarshaler.messageSize((boolean) anyValue.getValue(), pool);
      case LONG:
        return IntAnyValueMarshaler.messageSize((long) anyValue.getValue(), pool);
      case DOUBLE:
        return DoubleAnyValueMarshaler.messageSize((double) anyValue.getValue(), pool);
      case ARRAY:
        return ArrayAnyValueMarshaler.messageSize((List<AnyValue<?>>) anyValue.getValue(), pool);
      case KEY_VALUE_LIST:
        return KeyValueListAnyValueMarshaler.messageSize(
            (List<KeyAnyValue>) anyValue.getValue(), pool);
      case BYTES:
        return BytesAnyValueMarshaler.messageSize((ByteBuffer) anyValue.getValue(), pool);
    }
    throw new IllegalArgumentException("Unsupported AnyValue type: " + anyValue.getType());
  }

  @SuppressWarnings("unchecked")
  public static void encode(
      Serializer output,
      AnyValue<?> anyValue,
      MessageSize anyValueMessageSize,
      MarshallingObjectsPool pool)
      throws IOException {
    switch (anyValue.getType()) {
      case STRING:
        StringAnyValueMarshaler.encode(output, (String) anyValue.getValue());
        return;
      case BOOLEAN:
        BoolAnyValueMarshaler.encode(output, (boolean) anyValue.getValue());
        return;
      case LONG:
        IntAnyValueMarshaler.encode(output, (long) anyValue.getValue());
        return;
      case DOUBLE:
        DoubleAnyValueMarshaler.encode(output, (double) anyValue.getValue());
        return;
      case ARRAY:
        ArrayAnyValueMarshaler.encode(
            output,
            (List<AnyValue<?>>) anyValue.getValue(),
            anyValueMessageSize.getMessageTypeFieldSize(0),
            pool);
        return;
      case KEY_VALUE_LIST:
        KeyValueListAnyValueMarshaler.encode(
            output,
            (List<KeyAnyValue>) anyValue.getValue(),
            anyValueMessageSize.getMessageTypeFieldSize(0),
            pool);
        return;
      case BYTES:
        BytesAnyValueMarshaler.encode(output, (ByteBuffer) anyValue.getValue());
        return;
    }
    throw new IllegalArgumentException("Unsupported AnyValue type: " + anyValue.getType());
  }
}
