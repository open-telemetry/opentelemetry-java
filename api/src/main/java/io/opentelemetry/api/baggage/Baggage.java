/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ImplicitContextKeyed;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link String} to {@link String} and {@link EntryMetadata} that can be used to label
 * anything that is associated with a specific operation.
 *
 * <p>For example, {@code Baggage}s can be used to label stats, log messages, or debugging
 * information.
 */
@Immutable
public interface Baggage extends ImplicitContextKeyed {

  /** Baggage with no entries. */
  static Baggage empty() {
    return ImmutableBaggage.EMPTY;
  }

  /** Creates a new {@link BaggageBuilder} for creating Baggage. */
  static BaggageBuilder builder() {
    return ImmutableBaggage.builder();
  }

  /**
   * Returns Baggage from the current {@link Context}, falling back to empty Baggage if none is in
   * the current Context.
   */
  static Baggage current() {
    return fromContext(Context.current());
  }

  /**
   * Returns the {@link Baggage} from the specified {@link Context}, falling back to a empty {@link
   * Baggage} if there is no baggage in the context.
   */
  static Baggage fromContext(Context context) {
    Baggage baggage = fromContextOrNull(context);
    return baggage != null ? baggage : empty();
  }

  /**
   * Returns the {@link Baggage} from the specified {@link Context}, or {@code null} if there is no
   * baggage in the context.
   */
  @Nullable
  static Baggage fromContextOrNull(Context context) {
    return context.get(BaggageContextKey.KEY);
  }

  @Override
  default Context storeInContext(Context context) {
    return context.with(BaggageContextKey.KEY, this);
  }

  /**
   * Returns an immutable collection of the entries in this {@code Baggage}. Order of entries is not
   * guaranteed.
   *
   * @return an immutable collection of the entries in this {@code Baggage}.
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
   * Create a Builder pre-initialized with the contents of this Baggage. The returned Builder will
   * be set to not use an implicit parent, so any parent assignment must be done manually.
   */
  BaggageBuilder toBuilder();
}
