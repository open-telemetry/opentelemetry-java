/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

/**
 * Key for indexing values of type {@link T} stored in a {@link Context}. {@link ContextKey} are
 * compared by reference, so it is expected that only one {@link ContextKey} is created for a
 * particular type of context value.
 *
 * <pre>{@code
 * public class ContextUser {
 *
 *   private static final ContextKey<MyState> KEY = ContextKey.named("MyState");
 *
 *   public Context startWork() {
 *     return Context.withValues(KEY, new MyState());
 *   }
 *
 *   public void continueWork(Context context) {
 *     MyState state = context.getValue(KEY);
 *     // Keys are compared by reference only.
 *     assert state != Context.current().getValue(ContextKey.named("MyState"));
 *     ...
 *   }
 * }
 *
 * }</pre>
 */
// ErrorProne false positive, this is used for its type constraint, not only as a bag of statics.
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface ContextKey<T> {

  /**
   * Returns a new {@link ContextKey} with the given debug name. The name does not impact behavior
   * and is only for debugging purposes. Multiple different keys with the same name will be separate
   * keys.
   */
  static <T> ContextKey<T> named(String name) {
    return new DefaultContextKey<>(name);
  }
}
