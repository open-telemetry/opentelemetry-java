/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Key-value pair of {@link AttributeKey} key and its corresponding value.
 *
 * <p>Conceptually if {@link Attributes} is a Map, then this is a Map.Entry. Note that whilst {@link
 * KeyValue} is similar, this class holds type information on the Key rather than the value.
 */
public interface AttributeKeyValue<T> {

  /** Returns a {@link AttributeKeyValue} for the given {@link AttributeKey} and {@code value}. */
  static <T> AttributeKeyValue<T> of(AttributeKey<T> attributeKey, T value) {
    return AttributeKeyValueImpl.create(attributeKey, value);
  }

  /** Returns a List corresponding to the provided Map. This is a copy, not a view. */
  @SuppressWarnings("unchecked")
  static <T> List<AttributeKeyValue<?>> of(Attributes attributes) {
    List<AttributeKeyValue<?>> result = new ArrayList<>(attributes.size());
    attributes.forEach(
        (key, value) -> {
          result.add(of((AttributeKey<T>) key, (T) value));
        });
    return result;
  }

  /** Returns the key. */
  AttributeKey<T> getAttributeKey();

  /** Returns the value. */
  T getValue();
}
