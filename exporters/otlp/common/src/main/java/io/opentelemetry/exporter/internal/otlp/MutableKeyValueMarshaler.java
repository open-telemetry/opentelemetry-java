package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.extension.incubator.logs.KeyAnyValue;
import io.opentelemetry.sdk.internal.DynamicList;
import java.util.Collections;
import java.util.List;

public class MutableKeyValueMarshaler extends KeyValueMarshaler {
  private String key;
  private Marshaler value;

  private int size;

  public static ImmutableKeyValueMarshaler createForKeyAnyValue(
      KeyAnyValue keyAnyValue,
      MarshallerObjectPools marshallerObjectPools) {
    MutableKeyValueMarshaler mutableKeyValueMarshaler = marshallerObjectPools
        .getMutableKeyValueMarshallerPool()
        .borrowObject();

    mutableKeyValueMarshaler.set(
        keyAnyValue.getKey(),
        // TODO Asaf: Change to Mutable
        AnyValueMarshaler.create(keyAnyValue.getAnyValue()));
  }

  /** Returns Marshalers for the given Attributes. */
  @SuppressWarnings("AvoidObjectArrays")
  public static void createForAttributesIntoDynamicList(
      Attributes attributes,
      DynamicList<KeyValueMarshaler> keyValueMarshalersDynamicList,
      MarshallerObjectPools marshallerObjectPools) {
    if (attributes.isEmpty()) {
      keyValueMarshalersDynamicList.resizeAndClear(0);
    }

    keyValueMarshalersDynamicList.resizeAndClear(attributes.size());
    attributes.forEach(
        (attributeKey, o) ->
            keyValueMarshalersDynamicList.add(create(attributeKey, o, marshallerObjectPools)));
  }

  @SuppressWarnings("unchecked")
  private static KeyValueMarshaler create(
      AttributeKey<?> attributeKey,
      Object value,
      MarshallerObjectPools marshallerObjectPools) {

    MutableKeyValueMarshaler mutableKeyValueMarshaler = marshallerObjectPools
        .getMutableKeyValueMarshallerPool()
        .borrowObject();

    String key;
    if (attributeKey.getKey().isEmpty()) {
      key = "";
    } else {
      key = attributeKey.getKey();
    }
    switch (attributeKey.getType()) {
      case STRING:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to mutable
            key, StringAnyValueMarshaler.create((String) value));
        return mutableKeyValueMarshaler;
      case LONG:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, IntAnyValueMarshaler.create((long) value));
        return mutableKeyValueMarshaler;
      case BOOLEAN:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, BoolAnyValueMarshaler.create((boolean) value));
        return mutableKeyValueMarshaler;
      case DOUBLE:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, DoubleAnyValueMarshaler.create((double) value));
        return mutableKeyValueMarshaler;
      case STRING_ARRAY:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, ArrayAnyValueMarshaler.createString((List<String>) value));
        return mutableKeyValueMarshaler;
      case LONG_ARRAY:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, ArrayAnyValueMarshaler.createInt((List<Long>) value));
        return mutableKeyValueMarshaler;
      case BOOLEAN_ARRAY:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, ArrayAnyValueMarshaler.createBool((List<Boolean>) value));
        return mutableKeyValueMarshaler;
      case DOUBLE_ARRAY:
        mutableKeyValueMarshaler.set(
            // TODO Asaf: Change to immutable
            key, ArrayAnyValueMarshaler.createDouble((List<Double>) value));
        return mutableKeyValueMarshaler;
    }
    // Error-prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  void set(String key, Marshaler value) {
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
