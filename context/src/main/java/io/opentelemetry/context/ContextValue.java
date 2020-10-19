/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

/**
 * A value that can be stored inside {@link Context}. Types will generally use this interface to
 * allow storing themselves in {@link Context} without exposing a {@link ContextKey}.
 */
public interface ContextValue {

  /**
   * Returns a new {@link Context} created by setting {@code this} into the provided {@link
   * Context}. It is generally recommended to call {@link Context#withValues(ContextValue)} instead
   * of this method. The following are equivalent.
   *
   * <ul>
   *   <li>{@code context.withValues(myContextValue)}
   *   <li>{@code myContextValue.storeInContext(context)}
   * </ul>
   */
  Context storeInContext(Context context);
}
