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
    switch (value.type) {
      case STRING:
        return StringAnyValueMarshaler.create((String) value.value);
      case BOOLEAN:
        return BoolAnyValueMarshaler.create((boolean) value.value);
      case LONG:
        return IntAnyValueMarshaler.create((long) value.value);
      case DOUBLE:
        return DoubleAnyValueMarshaler.create((double) value.value);
      case ARRAY:
        return ArrayAnyValueMarshaler.createAnyValue((List<Value<?>>) value.value);
      case KEY_VALUE_LIST:
        return KeyValueListAnyValueMarshaler.create((List<KeyValue>) value.value);
      case BYTES:
        return BytesAnyValueMarshaler.create((ByteBuffer) value.value);
    }
    throw new IllegalArgumentException("Unsupported Value type: " + value.type);
  }
}
