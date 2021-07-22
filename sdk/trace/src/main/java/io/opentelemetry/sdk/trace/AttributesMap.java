/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** A map with a fixed capacity that drops attributes when the map gets full. */
final class AttributesMap extends HashMap<AttributeKey<?>, Object> implements Attributes {

  private static final long serialVersionUID = -5072696312123632376L;

  private final long capacity;
  private int totalAddedValues = 0;

  AttributesMap(long capacity) {
    this.capacity = capacity;
  }

  <T> void put(AttributeKey<T> key, T value) {
    totalAddedValues++;
    if (size() >= capacity && !containsKey(key)) {
      return;
    }
    super.put(key, value);
  }

  int getTotalAddedValues() {
    return totalAddedValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  @Override
  public Attributes removeAll(Attributes other) {
    AttributesMap result = new AttributesMap(capacity);
    result.putAll(this);
    other.forEach(this::remove);
    return result;
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    // Because Attributes is marked Immutable, IDEs may recognize this as redundant usage. However,
    // this class is private and is actually mutable, so we need to wrap with unmodifiableMap
    // anyways. We implement the immutable Attributes for this class to support the
    // Attributes.builder().putAll usage - it is tricky but an implementation detail of this private
    // class.
    return Collections.unmodifiableMap(this);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return Attributes.builder().putAll(this);
  }

  @Override
  public String toString() {
    return "AttributesMap{"
        + "data="
        + super.toString()
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  Attributes immutableCopy() {
    return Attributes.builder().putAll(this).build();
  }
}
