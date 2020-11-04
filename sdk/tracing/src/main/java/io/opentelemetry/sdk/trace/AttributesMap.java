/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.ReadableAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * A map with a fixed capacity that drops attributes when the map gets full.
 *
 * <p>Note: this doesn't implement the Map interface, but behaves very similarly to one.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class AttributesMap implements ReadableAttributes {
  private final Map<AttributeKey, Object> data = new HashMap<>();

  private final long capacity;
  private int totalAddedValues = 0;

  AttributesMap(long capacity) {
    this.capacity = capacity;
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
  public void forEach(AttributeConsumer consumer) {
    for (Map.Entry<AttributeKey, Object> entry : data.entrySet()) {
      AttributeKey key = entry.getKey();
      Object value = entry.getValue();
      consumer.accept(key, value);
    }
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

  @SuppressWarnings("rawtypes")
  ReadableAttributes immutableCopy() {
    AttributesBuilder builder = Attributes.builder();
    for (Map.Entry<AttributeKey, Object> entry : data.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }
}
