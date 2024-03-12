/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.incubator.logs.AnyValue;
import io.opentelemetry.api.incubator.logs.KeyAnyValue;
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
}
