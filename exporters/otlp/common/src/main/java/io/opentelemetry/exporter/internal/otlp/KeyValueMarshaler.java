/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.KeyAnyValue;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
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

  private static int calculateSize(byte[] keyUtf8, Marshaler value) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(KeyValue.KEY, keyUtf8);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, value);
    return size;
  }
}
