/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.ReadableAttributes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A map with a fixed capacity that drops attributes when the map gets full.
 *
 * <p>Note: this doesn't implement the Map interface, but behaves very similarly to one.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class AttributesMap implements ReadableAttributes {
  private final Map<AttributeKey<?>, Object> data;

  private final long capacity;
  private int totalAddedValues = 0;

  private AttributesMap(long capacity, Map<AttributeKey<?>, Object> data) {
    this.capacity = capacity;
    this.data = data;
  }

  AttributesMap(long capacity) {
    this(capacity, new LinkedHashMap<>());
  }

  public <T> void put(AttributeKey<T> key, T value) {
    if (key == null || key.getKey() == null || value == null) {
      return;
    }
    totalAddedValues++;
    if (data.size() >= capacity && !data.containsKey(key)) {
      return;
    }
    data.put(key, value);
  }

  int getTotalAddedValues() {
    return totalAddedValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(AttributeKey<T> key) {
    return (T) data.get(key);
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void forEach(BiConsumer<AttributeKey<?>, Object> consumer) {
    for (Map.Entry<AttributeKey<?>, Object> entry : data.entrySet()) {
      AttributeKey key = entry.getKey();
      Object value = entry.getValue();
      consumer.accept(key, value);
    }
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    return Collections.unmodifiableMap(data);
  }

  @Override
  public String toString() {
    return "AttributesMap{"
        + "data="
        + data
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  ReadableAttributes immutableCopy() {
    Map<AttributeKey<?>, Object> dataCopy = new LinkedHashMap<>(data);
    return new AttributesMap(capacity, Collections.unmodifiableMap(dataCopy));
  }
}
