/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Utility methods for accessing the {@link Baggage} contained in the {@link Context}. */
@Immutable
final class BaggageUtils {
  private static final ContextKey<Baggage> BAGGAGE_KEY =
      ContextKey.named("opentelemetry-baggage-key");

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param baggage the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   */
  static Context withBaggage(Baggage baggage, Context context) {
    return context.with(BAGGAGE_KEY, baggage);
  }

  /**
   * Returns the {@link Baggage} from the {@linkplain Context#current current context}, falling back
   * to an empty {@link Baggage}.
   *
   * @return the {@link Baggage} from the {@linkplain Context#current current context}.
   */
  static Baggage getCurrentBaggage() {
    return getBaggage(Context.current());
  }

  /**
   * Returns the {@link Baggage} from the specified {@code Context}, falling back to an empty {@link
   * Baggage}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Baggage} from the specified {@code Context}.
   */
  static Baggage getBaggage(Context context) {
    Baggage baggage = context.get(BAGGAGE_KEY);
    return baggage == null ? Baggage.empty() : baggage;
  }

  /**
   * Returns the {@link Baggage} from the specified {@code Context}. If none is found, this method
   * returns {code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Baggage} from the specified {@code Context}.
   */
  @Nullable
  static Baggage getBaggageWithoutDefault(Context context) {
    return context.get(BAGGAGE_KEY);
  }

  private BaggageUtils() {}
}
