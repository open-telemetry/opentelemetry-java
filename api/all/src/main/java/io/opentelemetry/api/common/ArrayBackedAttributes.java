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

  /** Default impelmentation for remove all method added to Attributes. */
  static Attributes removeAllImpl(Attributes source, Attributes toRemove) {
    if (toRemove.isEmpty()) {
      return source;
    }
    if (source instanceof ArrayBackedAttributes && toRemove instanceof ArrayBackedAttributes) {
      // Efficient O(N) implementation.
      return removeAllLinear((ArrayBackedAttributes) source, (ArrayBackedAttributes) toRemove);
    }
    // Default implementation.  Does one lookup on toRemove per key on source, plus a merge sort.
    return removeAllSlow(source, toRemove);
  }

  /** More efficient version of removeall when we know keys are sorted. */
  private static Attributes removeAllLinear(
      ArrayBackedAttributes source, ArrayBackedAttributes other) {
    final List<Object> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < source.size() * 2 && j < other.size() * 2) {
      int keyCompare =
          KEY_COMPARATOR_FOR_CONSTRUCTION.compare(
              (AttributeKey<?>) source.getRaw(i), (AttributeKey<?>) other.getRaw(j));
      if (keyCompare == 0) {
        // Match, drop our value, No-Match we still add it.
        if (!source.getRaw(i + 1).equals(other.getRaw(j + 1))) {
          result.add(source.getRaw(i));
          result.add(source.getRaw(i + 1));
        }
        i += 2;
        j += 2;
      } else if (keyCompare < 0) {
        // Our value is earlier, add it and move
        result.add(source.getRaw(i));
        result.add(source.getRaw(i + 1));
        i += 2;
      } else {
        // The other side's key isn't in our map, ignore it.
        j += 2;
      }
    }
    // Grab the rest of our attributes if we ended early.
    while (i < source.size() * 2) {
      result.add(source.getRaw(i));
      result.add(source.getRaw(i + 1));
      i += 2;
    }
    if (result.isEmpty()) {
      return EMPTY;
    }
    return new ArrayBackedAttributes(result.toArray(), KEY_COMPARATOR_FOR_CONSTRUCTION);
  }

  /** Default implementation to use. */
  private static Attributes removeAllSlow(Attributes source, Attributes other) {
    if (other.isEmpty()) {
      return source;
    }
    final List<Object> result = new ArrayList<>();
    source.forEach(
        (key, value) -> {
          if (!value.equals(other.get(key))) {
            result.add(key);
            result.add(value);
          }
        });
    if (result.isEmpty()) {
      return EMPTY;
    }
    return sortAndFilterToAttributes(result.toArray());
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
