/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ImplicitContextKeyed;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link String} to {@link String} and {@link EntryMetadata} that can be used to label
 * anything that is associated with a specific operation.
 *
 * <p>For example, {@code Baggage}s can be used to label stats, log messages, or debugging
 * information.
 *
 * <p>Implementations of this interface *must* be immutable and have well-defined value-based
 * equals/hashCode implementations. If an implementation does not strictly conform to these
 * requirements, behavior of the OpenTelemetry APIs and default SDK cannot be guaranteed.
 *
 * <p>For this reason, it is strongly suggested that you use the implementation that is provided
 * here via the factory methods and the {@link BaggageBuilder}.
 */
@Immutable
public interface Baggage extends ImplicitContextKeyed {

  /** Baggage with no entries. */
  static Baggage empty() {
    return ImmutableBaggage.empty();
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

  /** Returns the number of entries in this {@link Baggage}. */
  int size();

  /** Returns whether this {@link Baggage} is empty, containing no entries. */
  default boolean isEmpty() {
    return size() == 0;
  }

  /** Iterates over all the entries in this {@link Baggage}. */
  void forEach(BaggageConsumer consumer);

  /** Returns a read-only view of this {@link Baggage} as a {@link Map}. */
  Map<String, BaggageEntry> asMap();

  /**
   * Returns the {@code String} value associated with the given key, without metadata.
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
