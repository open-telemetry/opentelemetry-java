package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.AnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.ArrayAnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.BoolAnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.DoubleAnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.IntAnyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.StringAnyValueMarshaler;
import io.opentelemetry.extension.incubator.logs.KeyAnyValue;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class ImmutableKeyValueMarshaler extends KeyValueMarshaler {
  private final String key;
  private final Marshaler value;

  private final int size;

  public static ImmutableKeyValueMarshaler createForKeyAnyValue(KeyAnyValue keyAnyValue) {
    return new ImmutableKeyValueMarshaler(
        keyAnyValue.getKey(),
        // TODO Asaf: Change to immutable
        AnyValueMarshaler.create(keyAnyValue.getAnyValue()));
  }

  /** Returns Marshalers for the given Attributes. */
  @SuppressWarnings("AvoidObjectArrays")
  public static List<KeyValueMarshaler> createForAttributes(Attributes attributes) {
    if (attributes.isEmpty()) {
      return Collections.emptyList();
    }

    List<KeyValueMarshaler> marshalers = new ArrayList<>(attributes.size());
    attributes.forEach(
            (attributeKey, o) -> marshalers.add(create(attributeKey, o)));
    return marshalers;
  }

  @SuppressWarnings("unchecked")
  private static KeyValueMarshaler create(AttributeKey<?> attributeKey, Object value) {
    String key;
    if (attributeKey.getKey().isEmpty()) {
      key = "";
    } else {
      key = attributeKey.getKey();
    }
    switch (attributeKey.getType()) {
      case STRING:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, StringAnyValueMarshaler.create((String) value));
      case LONG:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, IntAnyValueMarshaler.create((long) value));
      case BOOLEAN:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, BoolAnyValueMarshaler.create((boolean) value));
      case DOUBLE:
        return new ImmutableKeyValueMarshaler(
            key, DoubleAnyValueMarshaler.create((double) value));
      case STRING_ARRAY:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, ArrayAnyValueMarshaler.createString((List<String>) value));
      case LONG_ARRAY:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, ArrayAnyValueMarshaler.createInt((List<Long>) value));
      case BOOLEAN_ARRAY:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, ArrayAnyValueMarshaler.createBool((List<Boolean>) value));
      case DOUBLE_ARRAY:
        // TODO Asaf: Change to immutable
        return new ImmutableKeyValueMarshaler(
            key, ArrayAnyValueMarshaler.createDouble((List<Double>) value));
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  private ImmutableKeyValueMarshaler(String key, Marshaler value) {
    this.key = key;
    this.value = value;
    this.size = calculateSize(key, value);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  String getKey() {
    return key;
  }

  @Override
  Marshaler getValue() {
    return value;
  }
}
