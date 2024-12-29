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
final class ArrayBackedExtendedAttributes
    extends ImmutableKeyValuePairs<ExtendedAttributeKey<?>, Object> implements ExtendedAttributes {

  // We only compare the key name, not type, when constructing, to allow deduping keys with the
  // same name but different type.
  private static final Comparator<ExtendedAttributeKey<?>> KEY_COMPARATOR_FOR_CONSTRUCTION =
      Comparator.comparing(ExtendedAttributeKey::getKey);

  static final ExtendedAttributes EMPTY = ExtendedAttributes.builder().build();

  @Nullable private Attributes attributes;

  private ArrayBackedExtendedAttributes(
      Object[] data, Comparator<ExtendedAttributeKey<?>> keyComparator) {
    super(data, keyComparator);
  }

  /**
   * Only use this constructor if you can guarantee that the data has been de-duped, sorted by key
   * and contains no null values or null/empty keys.
   *
   * @param data the raw data
   */
  ArrayBackedExtendedAttributes(Object[] data) {
    super(data);
  }

  @Override
  public ExtendedAttributesBuilder toBuilder() {
    return new ArrayBackedExtendedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(ExtendedAttributeKey<T> key) {
    return (T) super.get(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Attributes asAttributes() {
    if (attributes == null) {
      AttributesBuilder builder = Attributes.builder();
      forEach(
          (extendedAttributeKey, value) -> {
            AttributeKey<Object> attributeKey =
                (AttributeKey<Object>) extendedAttributeKey.asAttributeKey();
            if (attributeKey != null) {
              builder.put(attributeKey, value);
            }
          });
      attributes = builder.build();
    }
    return attributes;
  }

  static ExtendedAttributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      ExtendedAttributeKey<?> key = (ExtendedAttributeKey<?>) data[i];
      if (key != null && key.getKey().isEmpty()) {
        data[i] = null;
      }
    }
    return new ArrayBackedExtendedAttributes(data, KEY_COMPARATOR_FOR_CONSTRUCTION);
  }
}
