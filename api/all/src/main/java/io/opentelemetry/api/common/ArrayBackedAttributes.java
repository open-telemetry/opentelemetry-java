/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ArrayBackedAttributes extends ImmutableKeyValuePairs<AttributeKey<?>, Object>
    implements Attributes {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<AttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(AttributeKey::getKey);

  static final Attributes EMPTY = Attributes.builder().build();

  private ArrayBackedAttributes(Object[] data, Comparator<AttributeKey<?>> keyComparator) {
    super(data, keyComparator);
  }

  @Override
  public Attributes removeAll(Attributes other) {
    if (!(other instanceof ArrayBackedAttributes)) {
      throw new IllegalArgumentException("removeAll only implemented for built-in attributes");
    }
    final List<Object> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < size() && j < other.size()) {
      int keyCompare =
          KEY_COMPARATOR_FOR_CONSTRUCTION.compare(
              (AttributeKey<?>) getRaw(i),
              (AttributeKey<?>) ((ArrayBackedAttributes) other).getRaw(j));

      if (keyCompare == 0) {
        // Match, drop our value
        i += 2;
        j += 2;
      } else if (keyCompare > 0) {
        // Our value is earlier, add it and move
        result.add(getRaw(i));
        result.add(getRaw(i + 1));
        i += 2;
      } else {
        j += 2;
      }
    }
    if (result.isEmpty()) {
      return EMPTY;
    }
    return new ArrayBackedAttributes(result.toArray(), KEY_COMPARATOR_FOR_CONSTRUCTION);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return new ArrayBackedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  static Attributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && key.getKey().isEmpty()) {
        data[i] = null;
      }
    }
    return new ArrayBackedAttributes(data, KEY_COMPARATOR_FOR_CONSTRUCTION);
  }
}
