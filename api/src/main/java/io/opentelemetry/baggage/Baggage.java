/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import io.opentelemetry.context.Context;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link String} to {@link String} and {@link EntryMetadata} that can be used to label
 * anything that is associated with a specific operation.
 *
 * <p>For example, {@code Baggage}s can be used to label stats, log messages, or debugging
 * information.
 *
 * @since 0.9.0
 */
@Immutable
public interface Baggage {
  /**
   * Returns an immutable collection of the entries in this {@code Baggage}. Order of entries is not
   * guaranteed.
   *
   * @return an immutable collection of the entries in this {@code Baggage}.
   * @since 0.9.0
   */
  Collection<Entry> getEntries();

  /**
   * Returns the {@code String} associated with the given key.
   *
   * @param entryKey entry key to return the value for.
   * @return the value associated with the given key, or {@code null} if no {@code Entry} with the
   *     given {@code entryKey} is in this {@code Baggage}.
   */
  @Nullable
  String getEntryValue(String entryKey);

  /**
   * Builder for the {@link Baggage} class.
   *
   * @since 0.9.0
   */
  interface Builder {
    /**
     * Sets the parent {@link Baggage} to use from the specified {@code Context}. If no parent
     * {@link Baggage} is provided, the value of {@link BaggageManager#getCurrentBaggage()} at
     * {@link #build()} time will be used as parent, unless {@link #setNoParent()} was called.
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
     * @since 0.9.0
     */
    Builder setParent(Context context);

    /**
     * Sets the option to become a root {@link Baggage} with no parent. If <b>not</b> called, the
     * value provided using {@link #setParent(Context)} or otherwise {@link
     * BaggageManager#getCurrentBaggage()} at {@link #build()} time will be used as parent.
     *
     * @return this.
     * @since 0.9.0
     */
    Builder setNoParent();

    /**
     * Adds the key/value pair and metadata regardless of whether the key is present.
     *
     * @param key the {@code String} key which will be set.
     * @param value the {@code String} value to set for the given key.
     * @param entryMetadata the {@code EntryMetadata} associated with this {@link Entry}.
     * @return this
     * @since 0.9.0
     */
    Builder put(String key, String value, EntryMetadata entryMetadata);

    /**
     * Removes the key if it exists.
     *
     * @param key the {@code String} key which will be removed.
     * @return this
     * @since 0.9.0
     */
    Builder remove(String key);

    /**
     * Creates a {@code Baggage} from this builder.
     *
     * @return a {@code Baggage} with the same entries as this builder.
     * @since 0.9.0
     */
    Baggage build();
  }
}
