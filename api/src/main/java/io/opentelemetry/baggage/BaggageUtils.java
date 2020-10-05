/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import io.grpc.Context;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Utility methods for accessing the {@link Baggage} contained in the {@link io.grpc.Context}.
 *
 * @since 0.9.0
 */
@Immutable
public final class BaggageUtils {
  private static final Context.Key<Baggage> CORR_CONTEXT_KEY =
      Context.key("opentelemetry-corr-context-key");

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param baggage the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.9.0
   */
  public static Context withBaggage(Baggage baggage, Context context) {
    return context.withValue(CORR_CONTEXT_KEY, baggage);
  }

  /**
   * Returns the {@link Baggage} from the {@linkplain Context#current current context}, falling back
   * to an empty {@link Baggage}.
   *
   * @return the {@link Baggage} from the {@linkplain Context#current current context}.
   * @since 0.9.0
   */
  public static Baggage getCurrentBaggage() {
    return getBaggage(Context.current());
  }

  /**
   * Returns the {@link Baggage} from the specified {@code Context}, falling back to an empty {@link
   * Baggage}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Baggage} from the specified {@code Context}.
   * @since 0.9.0
   */
  public static Baggage getBaggage(Context context) {
    Baggage baggage = CORR_CONTEXT_KEY.get(context);
    return baggage == null ? EmptyBaggage.getInstance() : baggage;
  }

  /**
   * Returns the {@link Baggage} from the specified {@code Context}. If none is found, this method
   * returns {code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Baggage} from the specified {@code Context}.
   * @since 0.9.0
   */
  @Nullable
  public static Baggage getBaggageWithoutDefault(Context context) {
    return CORR_CONTEXT_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@link Baggage} added to the current
   * {@code Context}.
   *
   * @param baggage the {@link Baggage} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.9.0
   */
  public static Scope currentContextWith(Baggage baggage) {
    Context context = withBaggage(baggage, Context.current());
    return ContextUtils.withScopedContext(context);
  }

  private BaggageUtils() {}
}
