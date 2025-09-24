/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/**
 * A map with a fixed capacity that drops attributes when the map gets full, and which truncates
 * string and array string attribute values to the {@link #lengthLimit}.
 *
 * <p>WARNING: In order to reduce memory allocation, this class extends {@link HashMap} when it
 * would be more appropriate to delegate. The problem with extending is that we don't enforce that
 * all {@link HashMap} methods for reading / writing data conform to the configured attribute
 * limits. Therefore, it's easy to accidentally call something like {@link Map#putAll(Map)} and
 * bypass the restrictions (see <a
 * href="https://github.com/open-telemetry/opentelemetry-java/issues/7135">#7135</a>). Callers MUST
 * take care to only call methods from {@link AttributesMap}, and not {@link HashMap}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributesMap extends HashMap<AttributeKey<?>, Object> implements Attributes {

  private static final long serialVersionUID = -5072696312123632376L;

  private final long capacity;
  private final int lengthLimit;
  private int totalAddedValues = 0;

  private AttributesMap(long capacity, int lengthLimit) {
    this.capacity = capacity;
    this.lengthLimit = lengthLimit;
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
   */
  @Override
  @Nullable
  public Object put(AttributeKey<?> key, @Nullable Object value) {
    if (value == null) {
      return null;
    }
    totalAddedValues++;
    if (size() >= capacity && !containsKey(key)) {
      return null;
    }
    return super.put(key, AttributeUtil.applyAttributeLengthLimit(value, lengthLimit));
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
    return (T) super.get(key);
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
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> action) {
    // https://github.com/open-telemetry/opentelemetry-java/issues/4161
    // Help out android desugaring by having an explicit call to HashMap.forEach, when forEach is
    // just called through Attributes.forEach desugaring is unable to correctly handle it.
    super.forEach(action);
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

  /** Create an immutable copy of the attributes in this map. */
  public Attributes immutableCopy() {
    return Attributes.builder().putAll(this).build();
  }
}
