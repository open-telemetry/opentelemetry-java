/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Comparator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ArrayBackedComplexAttribute extends ImmutableKeyValuePairs<AttributeKey<?>, Object>
    implements ComplexAttribute {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<AttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(AttributeKey::getKey);

  static final ComplexAttribute EMPTY = ComplexAttribute.builder().build();

  private ArrayBackedComplexAttribute(Object[] data, Comparator<AttributeKey<?>> keyComparator) {
    super(data, keyComparator);
  }

  /**
   * Only use this constructor if you can guarantee that the data has been de-duped, sorted by key
   * and contains no null values or null/empty keys.
   *
   * @param data the raw data
   */
  ArrayBackedComplexAttribute(Object[] data) {
    super(data);
  }

  @Override
  public ComplexAttributeBuilder toBuilder() {
    return new ArrayBackedComplexAttributeBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  static ComplexAttribute sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && key.getKey().isEmpty()) {
        data[i] = null;
      }
    }
    return new ArrayBackedComplexAttribute(data, KEY_COMPARATOR_FOR_CONSTRUCTION);
  }
}
