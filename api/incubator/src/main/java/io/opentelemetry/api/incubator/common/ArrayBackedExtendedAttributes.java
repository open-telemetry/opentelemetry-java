/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@SuppressWarnings("deprecation")
@Immutable
final class ArrayBackedExtendedAttributes
    extends ImmutableKeyValuePairs<ExtendedAttributeKey<?>, Object> implements ExtendedAttributes {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<ExtendedAttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(ExtendedAttributeKey::getKey);

  static final ExtendedAttributes EMPTY = ExtendedAttributes.builder().build();

  @Nullable private Attributes attributes;

  private ArrayBackedExtendedAttributes(
      Object[] data, Comparator<ExtendedAttributeKey<?>> keyComparator) {
    super(data, keyComparator);
  }

  /**
   * Only use this constructor if you can guarantee that the data has been de-duped, sorted by key
   * and contains no null values or null/empty keys.
   *
   * @param data the raw data
   */
  ArrayBackedExtendedAttributes(Object[] data) {
    super(data);
  }

  @Override
  public ExtendedAttributesBuilder toBuilder() {
    return new ArrayBackedExtendedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(ExtendedAttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    if (key.getType() == ExtendedAttributeType.VALUE) {
      return (T) getAsValue(key.getKey());
    }
    // Check if we're looking for an array type but have a VALUE with empty array
    if (isArrayType(key.getType())) {
      T value = (T) super.get(key);
      if (value == null) {
        // Check if there's a VALUE with the same key that contains an empty array
        Value<?> valueAttr = getValueAttribute(key.getKey());
        if (valueAttr != null && isEmptyArray(valueAttr)) {
          return (T) Collections.emptyList();
        }
      }
      return value;
    }
    return (T) super.get(key);
  }

  private static boolean isArrayType(ExtendedAttributeType type) {
    return type == ExtendedAttributeType.STRING_ARRAY
        || type == ExtendedAttributeType.LONG_ARRAY
        || type == ExtendedAttributeType.DOUBLE_ARRAY
        || type == ExtendedAttributeType.BOOLEAN_ARRAY;
  }

  @Nullable
  private Value<?> getValueAttribute(String keyName) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      ExtendedAttributeKey<?> currentKey = (ExtendedAttributeKey<?>) data.get(i);
      if (currentKey.getKey().equals(keyName)
          && currentKey.getType() == ExtendedAttributeType.VALUE) {
        return (Value<?>) data.get(i + 1);
      }
    }
    return null;
  }

  private static boolean isEmptyArray(Value<?> value) {
    if (value.getType() != ValueType.ARRAY) {
      return false;
    }
    @SuppressWarnings("unchecked")
    List<Value<?>> arrayValues = (List<Value<?>>) value.getValue();
    return arrayValues.isEmpty();
  }

  @Nullable
  private Value<?> getAsValue(String keyName) {
    // Find any attribute with the same key name and convert it to Value
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      ExtendedAttributeKey<?> currentKey = (ExtendedAttributeKey<?>) data.get(i);
      if (currentKey.getKey().equals(keyName)) {
        Object value = data.get(i + 1);
        return asValue(currentKey.getType(), value);
      }
    }
    return null;
  }

  @SuppressWarnings({"unchecked", "deprecation"}) // deprecation: EXTENDED_ATTRIBUTES
  @Nullable
  private static Value<?> asValue(ExtendedAttributeType type, Object value) {
    switch (type) {
      case STRING:
        return Value.of((String) value);
      case LONG:
        return Value.of((Long) value);
      case DOUBLE:
        return Value.of((Double) value);
      case BOOLEAN:
        return Value.of((Boolean) value);
      case STRING_ARRAY:
        List<String> stringList = (List<String>) value;
        Value<?>[] stringValues = new Value<?>[stringList.size()];
        for (int i = 0; i < stringList.size(); i++) {
          stringValues[i] = Value.of(stringList.get(i));
        }
        return Value.of(stringValues);
      case LONG_ARRAY:
        List<Long> longList = (List<Long>) value;
        Value<?>[] longValues = new Value<?>[longList.size()];
        for (int i = 0; i < longList.size(); i++) {
          longValues[i] = Value.of(longList.get(i));
        }
        return Value.of(longValues);
      case DOUBLE_ARRAY:
        List<Double> doubleList = (List<Double>) value;
        Value<?>[] doubleValues = new Value<?>[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
          doubleValues[i] = Value.of(doubleList.get(i));
        }
        return Value.of(doubleValues);
      case BOOLEAN_ARRAY:
        List<Boolean> booleanList = (List<Boolean>) value;
        Value<?>[] booleanValues = new Value<?>[booleanList.size()];
        for (int i = 0; i < booleanList.size(); i++) {
          booleanValues[i] = Value.of(booleanList.get(i));
        }
        return Value.of(booleanValues);
      case VALUE:
        // Already a Value
        return (Value<?>) value;
      case EXTENDED_ATTRIBUTES:
        // Cannot convert EXTENDED_ATTRIBUTES to Value
        return null;
    }
    // Should not reach here
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Attributes asAttributes() {
    if (attributes == null) {
      AttributesBuilder builder = Attributes.builder();
      forEach(
          (extendedAttributeKey, value) -> {
            AttributeKey<Object> attributeKey =
                (AttributeKey<Object>) extendedAttributeKey.asAttributeKey();
            if (attributeKey != null) {
              builder.put(attributeKey, value);
            }
          });
      attributes = builder.build();
    }
    return attributes;
  }

  static ExtendedAttributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      ExtendedAttributeKey<?> key = (ExtendedAttributeKey<?>) data[i];
      if (key != null && key.getKey().isEmpty()) {
        data[i] = null;
      }
    }
    return new ArrayBackedExtendedAttributes(data, KEY_COMPARATOR_FOR_CONSTRUCTION);
  }
}
