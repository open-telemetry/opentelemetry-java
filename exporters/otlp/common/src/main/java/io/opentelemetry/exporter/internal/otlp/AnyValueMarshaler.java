/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
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
  public static MarshalerWithSize create(Value<?> value) {
    switch (value.getType()) {
      case STRING:
        return StringAnyValueMarshaler.create((String) value.getValue());
      case BOOLEAN:
        return BoolAnyValueMarshaler.create((boolean) value.getValue());
      case LONG:
        return IntAnyValueMarshaler.create((long) value.getValue());
      case DOUBLE:
        return DoubleAnyValueMarshaler.create((double) value.getValue());
      case ARRAY:
        return ArrayAnyValueMarshaler.createAnyValue((List<Value<?>>) value.getValue());
      case KEY_VALUE_LIST:
        return KeyValueListAnyValueMarshaler.create((List<KeyValue>) value.getValue());
      case BYTES:
        return BytesAnyValueMarshaler.create((ByteBuffer) value.getValue());
      case EMPTY:
        return EmptyAnyValueMarshaler.INSTANCE;
    }
    throw new IllegalArgumentException("Unsupported Value type: " + value.getType());
  }
}
