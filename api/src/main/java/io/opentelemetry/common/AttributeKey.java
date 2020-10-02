/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * This interface provides a handle for setting the values of {@link Attributes}. The type of value
 * that can be set with an implementation of this key is denoted by the type parameter.
 *
 * <p>Implementations MUST be immutable, as these are used as the keys to Maps.
 *
 * @param <T> The type of value that can be set with the key.
 */
@SuppressWarnings("rawtypes")
@Immutable
public interface AttributeKey<T> extends Comparable<AttributeKey> {
  /** Returns the underlying String representation of the key. */
  String getKey();

  /** Returns the type of attribute for this key. Useful for building switch statements. */
  @Nonnull
  AttributeType getType();
}
