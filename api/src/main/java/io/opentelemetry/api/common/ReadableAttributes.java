/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A read-only container for String-keyed attributes.
 *
 * <p>See {@link Attributes} for the public API implementation.
 */
public interface ReadableAttributes {
  <T> T get(AttributeKey<T> key);

  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(BiConsumer<AttributeKey<?>, Object> consumer);

  /**
   * Returns a read-only view of this {@link io.opentelemetry.api.common.ReadableAttributes} as a
   * {@link Map}.
   */
  Map<AttributeKey<?>, Object> asMap();
}
