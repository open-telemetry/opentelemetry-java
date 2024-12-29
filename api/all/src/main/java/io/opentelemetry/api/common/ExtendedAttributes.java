/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@SuppressWarnings("rawtypes")
@Immutable
public interface ExtendedAttributes {

  /** Returns the value for the given {@link AttributeKey}, or {@code null} if not found. */
  @Nullable
  default <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    return get(key.asExtendedAttributeKey());
  }

  /** Returns the value for the given {@link ExtendedAttributeKey}, or {@code null} if not found. */
  @Nullable
  <T> T get(ExtendedAttributeKey<T> key);

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(BiConsumer<? super ExtendedAttributeKey<?>, ? super Object> consumer);

  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /** Returns a read-only view of this {@link ExtendedAttributes} as a {@link Map}. */
  Map<ExtendedAttributeKey<?>, Object> asMap();

  /**
   * Return a view of this extended attributes with entries limited to those representable as
   * standard attributes.
   */
  Attributes asAttributes();

  /** Returns a {@link ExtendedAttributes} instance with no attributes. */
  static ExtendedAttributes empty() {
    return ArrayBackedExtendedAttributes.EMPTY;
  }

  /**
   * Returns a new {@link ExtendedAttributesBuilder} instance for creating arbitrary {@link
   * ExtendedAttributes}.
   */
  static ExtendedAttributesBuilder builder() {
    return new ArrayBackedExtendedAttributesBuilder();
  }

  /**
   * Returns a new {@link ExtendedAttributesBuilder} instance populated with the data of this {@link
   * ExtendedAttributes}.
   */
  ExtendedAttributesBuilder toBuilder();
}
