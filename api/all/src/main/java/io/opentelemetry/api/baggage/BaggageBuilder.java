/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;

/**
 * A builder of {@link Baggage}.
 *
 * @see Baggage#builder()
 */
public interface BaggageBuilder {

  /**
   * Sets the parent {@link Baggage} to use from the specified {@code Context}. If no parent {@link
   * Baggage} is provided, the value of {@link Baggage#current()} at {@link #build()} time will be
   * used as parent, unless {@link #setNoParent()} was called.
   *
   * <p>If no parent {@link Baggage} is available in the specified {@code Context}, the resulting
   * {@link Baggage} will become a root instance, as if {@link #setNoParent()} had been called.
   *
   * <p>This <b>must</b> be used to create a {@link Baggage} when manual Context propagation is
   * used.
   *
   * <p>If called multiple times, only the last specified value will be used.
   *
   * @param context the {@code Context}.
   * @return this.
   * @throws NullPointerException if {@code context} is {@code null}.
   * @see #setNoParent()
   */
  BaggageBuilder setParent(Context context);

  /**
   * Sets the option to become a root {@link Baggage} with no parent. If <b>not</b> called, the
   * value provided using {@link #setParent(Context)} or otherwise {@link Baggage#current()} at
   * {@link #build()} time will be used as parent.
   *
   * @return this.
   */
  BaggageBuilder setNoParent();

  /**
   * Adds the key/value pair and metadata regardless of whether the key is present.
   *
   * @param key the {@code String} key which will be set.
   * @param value the {@code String} value to set for the given key.
   * @param entryMetadata the {@code EntryMetadata} associated with this {@link Entry}.
   * @return this
   */
  BaggageBuilder put(String key, String value, BaggageEntryMetadata entryMetadata);

  /**
   * Adds the key/value pair with empty metadata regardless of whether the key is present.
   *
   * @param key the {@code String} key which will be set.
   * @param value the {@code String} value to set for the given key.
   * @return this
   */
  default BaggageBuilder put(String key, String value) {
    return put(key, value, BaggageEntryMetadata.empty());
  }

  /**
   * Removes the key if it exists.
   *
   * @param key the {@code String} key which will be removed.
   * @return this
   */
  BaggageBuilder remove(String key);

  /**
   * Creates a {@code Baggage} from this builder.
   *
   * @return a {@code Baggage} with the same entries as this builder.
   */
  Baggage build();
}
