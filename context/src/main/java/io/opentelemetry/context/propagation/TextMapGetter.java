/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * Interface that allows a {@code TextMapPropagator} to read propagated fields from a carrier.
 *
 * <p>{@code Getter} is stateless and allows to be saved as a constant to avoid runtime allocations.
 *
 * @param <C> carrier of propagation fields, such as an http request.
 */
public interface TextMapGetter<C> {

  /**
   * Returns all the keys in the given carrier.
   *
   * @param carrier carrier of propagation fields, such as an http request.
   * @since 0.10.0
   */
  Iterable<String> keys(C carrier);

  /**
   * Returns the first value of the given propagation {@code key} or returns {@code null}.
   *
   * @param carrier carrier of propagation fields, such as an http request.
   * @param key the key of the field.
   * @return the first value of the given propagation {@code key} or returns {@code null}.
   */
  @Nullable
  String get(@Nullable C carrier, String key);

  /**
   * If implemented, returns all values for a given {@code key} in order, or returns an empty list.
   *
   * <p>The default method returns the first value of the given propagation {@code key} as a
   * singleton list, or returns an empty list.
   *
   * @param carrier carrier of propagation fields, such as an http request.
   * @param key the key of the field.
   * @return all values for a given {@code key} in order, or returns an empty list. Default method
   *     wraps {@code get()} as an {@link Iterator}.
   * @since 1.50.0
   */
  default Iterator<String> getAll(@Nullable C carrier, String key) {
    String first = get(carrier, key);
    if (first == null) {
      return Collections.emptyIterator();
    }
    return Collections.singleton(first).iterator();
  }
}
