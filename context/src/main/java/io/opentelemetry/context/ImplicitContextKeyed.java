/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import com.google.errorprone.annotations.MustBeClosed;

/**
 * A value that can be stored inside {@link Context}. Types will generally use this interface to
 * allow storing themselves in {@link Context} without exposing a {@link ContextKey}.
 */
public interface ImplicitContextKeyed {

  /**
   * Adds this {@link ImplicitContextKeyed} value to the {@link Context#current() current context}
   * and makes the new {@link Context} the current context. {@link Scope#close()} must be called to
   * properly restore the previous context from before this scope of execution or context will not
   * work correctly. It is recommended to use try-with-resources to call {@link Scope#close()}
   * automatically.
   *
   * <p>This method is equivalent to {@code Context.current().with(value).makeCurrent()}.
   *
   * <p>The default implementation of this method will store the {@link ImplicitContextKeyed} in a
   * {@link ThreadLocal}. Kotlin coroutine users SHOULD NOT use this method as the {@link
   * ThreadLocal} will not be properly synced across coroutine suspension and resumption. Instead,
   * use {@code withContext(value.asContextElement())} provided by the {@code
   * opentelemetry-extension-kotlin} library.
   */
  @MustBeClosed
  default Scope makeCurrent() {
    return Context.current().with(this).makeCurrent();
  }

  /**
   * Returns a new {@link Context} created by setting {@code this} into the provided {@link
   * Context}. It is generally recommended to call {@link Context#with(ImplicitContextKeyed)}
   * instead of this method. The following are equivalent.
   *
   * <ul>
   *   <li>{@code context.with(myContextValue)}
   *   <li>{@code myContextValue.storeInContext(context)}
   * </ul>
   */
  Context storeInContext(Context context);
}
