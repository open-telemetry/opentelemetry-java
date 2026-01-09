/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.incubator.common.ExtendedAttributesBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/**
 * A map with a fixed capacity that drops attributes when the map gets full, and which truncates
 * string and array string attribute values to the {@link #lengthLimit}.
 *
 * <p>{@link ExtendedAttributes} analog of {@link AttributesMap}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExtendedAttributesMap extends HashMap<ExtendedAttributeKey<?>, Object>
    implements ExtendedAttributes {

  private static final long serialVersionUID = -2674974862318200501L;

  private final long capacity;
  private final int lengthLimit;
  private int totalAddedValues = 0;

  private ExtendedAttributesMap(long capacity, int lengthLimit) {
    this.capacity = capacity;
    this.lengthLimit = lengthLimit;
  }

  /**
   * Create an instance.
   *
   * @param capacity the max number of extended attribute entries
   * @param lengthLimit the maximum length of string attributes
   */
  public static ExtendedAttributesMap create(long capacity, int lengthLimit) {
    return new ExtendedAttributesMap(capacity, lengthLimit);
  }

  /** Add the attribute key value pair, applying capacity and length limits. */
  @Override
  @Nullable
  public Object put(ExtendedAttributeKey<?> key, @Nullable Object value) {
    if (value == null) {
      return null;
    }
    totalAddedValues++;
    if (size() >= capacity && !containsKey(key)) {
      return null;
    }
    return super.put(key, AttributeUtil.applyAttributeLengthLimit(value, lengthLimit));
  }

  public <T> void putIfCapacity(ExtendedAttributeKey<T> key, @Nullable T value) {
    put(key, value);
  }

  /**
   * Get the total number of extended attributes added, including those dropped for capacity limits.
   */
  public int getTotalAddedValues() {
    return totalAddedValues;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T get(ExtendedAttributeKey<T> key) {
    return (T) super.get(key);
  }

  @Override
  public Map<ExtendedAttributeKey<?>, Object> asMap() {
    // Because ExtendedAttributes is marked Immutable, IDEs may recognize this as redundant usage.
    // However, this class is private and is actually mutable, so we need to wrap with
    // unmodifiableMap anyways. We implement the immutable ExtendedAttributes for this class to
    // support the ExtendedAttributes.builder().putAll usage - it is tricky but an implementation
    // detail of this private class.
    return Collections.unmodifiableMap(this);
  }

  @Override
  public ExtendedAttributesBuilder toBuilder() {
    return ExtendedAttributes.builder().putAll(this);
  }

  @Override
  public void forEach(BiConsumer<? super ExtendedAttributeKey<?>, ? super Object> action) {
    // https://github.com/open-telemetry/opentelemetry-java/issues/4161
    // Help out android desugaring by having an explicit call to HashMap.forEach, when forEach is
    // just called through ExtendedAttributes.forEach desugaring is unable to correctly handle it.
    super.forEach(action);
  }

  @Override
  public Attributes asAttributes() {
    return immutableCopy().asAttributes();
  }

  @Override
  public String toString() {
    return "ExtendedAttributesMap{"
        + "data="
        + super.toString()
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  /** Create an immutable copy of the extended attributes in this map. */
  public ExtendedAttributes immutableCopy() {
    return ExtendedAttributes.builder().putAll(this).build();
  }
}
