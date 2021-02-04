/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ArrayBackedAttributes extends ImmutableKeyValuePairs<AttributeKey<?>, Object>
    implements Attributes {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<AttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(AttributeKey::getKey);

  static final Attributes EMPTY = Attributes.builder().build();

  ArrayBackedAttributes(List<Object> data) {
    super(data);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return new ArrayBackedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  static Attributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && (key.getKey() == null || "".equals(key.getKey()))) {
        data[i] = null;
      }
    }
    return new ArrayBackedAttributes(
        sortAndFilter(data, /* filterNullValues= */ true, KEY_COMPARATOR_FOR_CONSTRUCTION));
  }
}
