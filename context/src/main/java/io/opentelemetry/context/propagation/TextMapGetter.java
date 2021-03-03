/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

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
}
