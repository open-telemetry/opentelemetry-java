/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.internal.AttributeValueLimits;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

class ArrayBackedAttributesBuilder implements AttributesBuilder {
  private final List<Object> data;

  /** Max number of unique entries. {@link Integer#MAX_VALUE} means unlimited. */
  private final int countLimit;

  /** Max length of string / byte-array values. {@link Integer#MAX_VALUE} means unlimited. */
  private final int valueLengthLimit;

  /** Max nesting depth for array / map values. {@link Integer#MAX_VALUE} means unlimited. */
  private final int valueDepthLimit;

  /** Count of non-null entries currently stored (excludes null holes from remove). */
  private int size;

  /**
   * Cached {@link #build()} result; null when unset or after a mutation. Only maintained when
   * limits are configured.
   */
  @Nullable private Attributes cachedBuild;

  ArrayBackedAttributesBuilder() {
    this(new ArrayList<>(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
  }

  ArrayBackedAttributesBuilder(List<Object> data) {
    this(data, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, data.size() / 2);
  }

  ArrayBackedAttributesBuilder(AttributeLimits limits) {
    this(
        new ArrayList<>(),
        limits.getCountLimit(),
        limits.getValueLengthLimit(),
        limits.getValueDepthLimit(),
        0);
  }

  private ArrayBackedAttributesBuilder(
      List<Object> data,
      int countLimit,
      int valueLengthLimit,
      int valueDepthLimit,
      int initialSize) {
    this.data = data;
    this.countLimit = countLimit;
    this.valueLengthLimit = valueLengthLimit;
    this.valueDepthLimit = valueDepthLimit;
    this.size = initialSize;
  }

  private boolean isLimited() {
    return countLimit != Integer.MAX_VALUE
        || valueLengthLimit != Integer.MAX_VALUE
        || valueDepthLimit != Integer.MAX_VALUE;
  }

  @Override
  public Attributes build() {
    Attributes cached = cachedBuild;
    if (cached != null) {
      return cached;
    }
    Attributes result;
    // If only one key-value pair AND the entry hasn't been set to null (by #remove(AttributeKey<T>)
    // or #removeIf(Predicate<AttributeKey<?>>)), then we can bypass sorting and filtering
    if (data.size() == 2 && data.get(0) != null) {
      result = new ArrayBackedAttributes(data.toArray());
    } else {
      result = ArrayBackedAttributes.sortAndFilterToAttributes(data.toArray());
    }
    if (isLimited()) {
      cachedBuild = result;
    }
    return result;
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
    return put(key, (long) value);
  }

  @Override
  public <T> AttributesBuilder put(AttributeKey<T> key, @Nullable T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (key.getType() == AttributeType.VALUE && value instanceof Value) {
      putValue(key, (Value<?>) value);
      return this;
    }
    addPair(key, value);
    return this;
  }

  /** Append (unlimited) or dedup-by-name / truncate / capacity-check (limited). */
  private void addPair(AttributeKey<?> key, Object value) {
    if (!isLimited()) {
      data.add(key);
      data.add(value);
      size++;
      return;
    }
    cachedBuild = null;
    Object limited = AttributeValueLimits.apply(value, valueLengthLimit, valueDepthLimit);
    String name = key.getKey();
    int emptySlot = -1;
    for (int i = 0; i < data.size(); i += 2) {
      Object existing = data.get(i);
      if (existing == null) {
        if (emptySlot < 0) {
          emptySlot = i;
        }
        continue;
      }
      if (((AttributeKey<?>) existing).getKey().equals(name)) {
        data.set(i, key);
        data.set(i + 1, limited);
        return;
      }
    }
    if (size >= countLimit) {
      return;
    }
    if (emptySlot >= 0) {
      data.set(emptySlot, key);
      data.set(emptySlot + 1, limited);
    } else {
      data.add(key);
      data.add(limited);
    }
    size++;
  }

  @SuppressWarnings("unchecked")
  private void putValue(AttributeKey<?> key, Value<?> valueObj) {
    // Convert VALUE type to narrower type when possible
    String keyName = key.getKey();
    switch (valueObj.getType()) {
      case STRING:
        put(stringKey(keyName), ((Value<String>) valueObj).getValue());
        return;
      case LONG:
        put(longKey(keyName), ((Value<Long>) valueObj).getValue());
        return;
      case DOUBLE:
        put(doubleKey(keyName), ((Value<Double>) valueObj).getValue());
        return;
      case BOOLEAN:
        put(booleanKey(keyName), ((Value<Boolean>) valueObj).getValue());
        return;
      case ARRAY:
        List<Value<?>> arrayValues = (List<Value<?>>) valueObj.getValue();
        AttributeType attributeType = attributeType(arrayValues);
        switch (attributeType) {
          case STRING_ARRAY:
            List<String> strings = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              strings.add((String) v.getValue());
            }
            put(stringArrayKey(keyName), strings);
            return;
          case LONG_ARRAY:
            List<Long> longs = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              longs.add((Long) v.getValue());
            }
            put(longArrayKey(keyName), longs);
            return;
          case DOUBLE_ARRAY:
            List<Double> doubles = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              doubles.add((Double) v.getValue());
            }
            put(doubleArrayKey(keyName), doubles);
            return;
          case BOOLEAN_ARRAY:
            List<Boolean> booleans = new ArrayList<>(arrayValues.size());
            for (Value<?> v : arrayValues) {
              booleans.add((Boolean) v.getValue());
            }
            put(booleanArrayKey(keyName), booleans);
            return;
          case VALUE:
            // Not coercible (empty, non-homogeneous, or unsupported element type)
            addPair(key, valueObj);
            return;
          default:
            throw new IllegalArgumentException("Unexpected array attribute type: " + attributeType);
        }
      case KEY_VALUE_LIST:
      case BYTES:
      case EMPTY:
        // Keep as VALUE type
        addPair(key, valueObj);
    }
  }

  /**
   * Returns the AttributeType for a homogeneous array (STRING_ARRAY, LONG_ARRAY, DOUBLE_ARRAY, or
   * BOOLEAN_ARRAY), or VALUE if the array is empty, non-homogeneous, or contains unsupported
   * element types.
   */
  private static AttributeType attributeType(List<Value<?>> arrayValues) {
    if (arrayValues.isEmpty()) {
      return AttributeType.VALUE;
    }
    ValueType elementType = arrayValues.get(0).getType();
    for (Value<?> v : arrayValues) {
      if (v.getType() != elementType) {
        return AttributeType.VALUE;
      }
    }
    switch (elementType) {
      case STRING:
        return AttributeType.STRING_ARRAY;
      case LONG:
        return AttributeType.LONG_ARRAY;
      case DOUBLE:
        return AttributeType.DOUBLE_ARRAY;
      case BOOLEAN:
        return AttributeType.BOOLEAN_ARRAY;
      case ARRAY:
      case KEY_VALUE_LIST:
      case BYTES:
      case EMPTY:
        return AttributeType.VALUE;
    }
    throw new IllegalArgumentException("Unsupported element type: " + elementType);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  // Safe: Attributes guarantees iteration over matching AttributeKey<T> / value pairs.
  public AttributesBuilder putAll(Attributes attributes) {
    if (attributes == null) {
      return this;
    }
    attributes.forEach((key, value) -> put((AttributeKey) key, value));
    return this;
  }

  @Override
  public <T> AttributesBuilder remove(AttributeKey<T> key) {
    if (key == null || key.getKey().isEmpty()) {
      return this;
    }
    return removeIf(
        entryKey ->
            key.getKey().equals(entryKey.getKey()) && key.getType().equals(entryKey.getType()));
  }

  @Override
  public AttributesBuilder removeIf(Predicate<AttributeKey<?>> predicate) {
    if (predicate == null) {
      return this;
    }
    for (int i = 0; i < data.size() - 1; i += 2) {
      Object entry = data.get(i);
      if (entry instanceof AttributeKey && predicate.test((AttributeKey<?>) entry)) {
        // null items are filtered out in ArrayBackedAttributes
        data.set(i, null);
        data.set(i + 1, null);
        size--;
        cachedBuild = null;
      }
    }
    return this;
  }

  static List<Double> toList(double... values) {
    Double[] boxed = new Double[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  static List<Long> toList(long... values) {
    Long[] boxed = new Long[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  static List<Boolean> toList(boolean... values) {
    Boolean[] boxed = new Boolean[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }
}
