/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import io.opentelemetry.context.Scope;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Object for creating new {@link Baggage}s and {@code Baggage}s based on the current context.
 *
 * <p>This class returns {@link Baggage.Builder builders} that can be used to create the
 * implementation-dependent {@link Baggage}s.
 *
 * <p>Implementations may have different constraints and are free to convert entry contexts to their
 * own subtypes. This means callers cannot assume the {@link #getCurrentBaggage() current context}
 * is the same instance as the one {@linkplain #withBaggage(Baggage) placed into scope}.
 *
 * @since 0.9.0
 */
@ThreadSafe
public interface BaggageManager {

  /**
   * Returns the current {@code Baggage}.
   *
   * @return the current {@code Baggage}.
   * @since 0.9.0
   */
  Baggage getCurrentBaggage();

  /**
   * Returns a new {@link Baggage.Builder}.
   *
   * @return a new {@code Baggage.Builder}.
   * @since 0.9.0
   */
  Baggage.Builder baggageBuilder();

  /**
   * Enters the scope of code where the given {@code Baggage} is in the current context (replacing
   * the previous {@code Baggage}) and returns an object that represents that scope. The scope is
   * exited when the returned object is closed.
   *
   * @param baggage the {@code Baggage} to be set as the current context.
   * @return an object that defines a scope where the given {@code Baggage} is set as the current
   *     context.
   * @since 0.9.0
   */
  Scope withBaggage(Baggage baggage);
}
