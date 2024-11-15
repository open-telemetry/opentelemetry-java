/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.internal.propagation;

import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * Extends {@link TextMapGetter} to return possibly multiple values for a given key.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @param <C> carrier of propagation fields, such as an http request.
 */
public interface ExtendedTextMapGetter<C> extends TextMapGetter<C> {
  /**
   * If implemented, returns all values for a given {@code key} in order, or returns an empty list.
   *
   * <p>The default method returns the first value of the given propagation {@code key} as a
   * singleton list, or returns an empty list.
   *
   * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
   * at any time.
   *
   * @param carrier carrier of propagation fields, such as an http request.
   * @param key the key of the field.
   * @return all values for a given {@code key} in order, or returns an empty list. Default method
   *     wraps {@code get()} as an {@link Iterator}.
   */
  default Iterator<String> getAll(@Nullable C carrier, String key) {
    String first = get(carrier, key);
    return Collections.singleton(first).iterator();
  }
}
