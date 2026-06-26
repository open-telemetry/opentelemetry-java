/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/**
 * A map with a fixed capacity that drops attributes when the map gets full, and which truncates
 * string and array string attribute values to the {@link #lengthLimit}.
 *
 * <p>Keyed internally by raw string attribute name, so that attributes with the same name but
 * different types are treated as the same key (last-value-wins), consistent with the OpenTelemetry
 * specification.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributesMap implements Attributes {

  private final long capacity;
  private final int lengthLimit;
  private int totalAddedValues = 0;

  private final LinkedHashMap<String, AttributeEntry> data;

  private static final class AttributeEntry {
    AttributeKey<?> key;
    Object value;

    AttributeEntry(AttributeKey<?> key, Object value) {
      this.key = key;
      this.value = value;
    }
  }

  private AttributesMap(long capacity, int lengthLimit) {
    this.capacity = capacity;
    this.lengthLimit = lengthLimit;
    this.data = new LinkedHashMap<>();
  }

  /**
   * Create an instance.
   *
   * @param capacity the max number of attribute entries
   * @param lengthLimit the maximum length of string attributes
   */
  public static AttributesMap create(long capacity, int lengthLimit) {
    return new AttributesMap(capacity, lengthLimit);
  }

  /**
   * Add the attribute key value pair, applying capacity and length limits. Callers MUST ensure the
   * {@code value} type matches the type required by {@code key}.
   *
   * <p>If an attribute with the same string key name already exists (regardless of type), it is
   * overwritten — last-value-wins, consistent with the OTel spec.
   */
  @Nullable
  public Object put(AttributeKey<?> key, @Nullable Object value) {
    if (value == null) {
      return null;
    }
    totalAddedValues++;
    String name = key.getKey();
    AttributeEntry entry = data.get(name);
    if (entry == null && data.size() >= capacity) {
      return null;
    }
    Object limited = AttributeUtil.applyAttributeLengthLimit(value, lengthLimit);
    if (entry == null) {
      data.put(name, new AttributeEntry(key, limited));
      return null;
    }
    Object old = entry.value;
    entry.key = key;
    entry.value = limited;
    return old;
  }

  /** Generic overload of {@link #put(AttributeKey, Object)}. */
  public <T> void putIfCapacity(AttributeKey<T> key, @Nullable T value) {
    put(key, value);
  }

  /** Get the total number of attributes added, including those dropped for capacity limits. */
  public int getTotalAddedValues() {
    return totalAddedValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(AttributeKey<T> key) {
    AttributeEntry entry = data.get(key.getKey());
    if (entry == null || !entry.key.getType().equals(key.getType())) {
      return null;
    }
    return (T) entry.value;
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    Map<AttributeKey<?>, Object> snapshot = new HashMap<>(data.size());
    data.values().forEach(e -> snapshot.put(e.key, e.value));
    return Collections.unmodifiableMap(snapshot);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return Attributes.builder().putAll(this);
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> action) {
    data.forEach((name, entry) -> action.accept(entry.key, entry.value));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attributes)) {
      return false;
    }
    return asMap().equals(((Attributes) o).asMap());
  }

  @Override
  public int hashCode() {
    return asMap().hashCode();
  }

  @Override
  public String toString() {
    return "AttributesMap{"
        + "data="
        + asMap()
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  /** Create an immutable copy of the attributes in this map. */
  public Attributes immutableCopy() {
    return Attributes.builder().putAll(this).build();
  }
}
