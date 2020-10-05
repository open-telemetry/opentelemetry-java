/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;

/**
 * A context propagation mechanism which can carry scoped-values across API boundaries and between
 * threads.
 *
 * <p>A Context object can be {@linkplain #makeCurrent set} to the {@link ContextStorage}, which
 * effectively forms a <b>scope</b> for the context. The scope is bound to the current thread.
 * Within a scope, its Context is accessible even across API boundaries, through {@link #current}.
 * The scope is later exited by {@link Scope#close()} closing} the scope.
 *
 * <p>Context objects are immutable and inherit state from their parent. To add or overwrite the
 * current state a new context object must be created and then attached, replacing the previously
 * bound context. For example:
 *
 * <pre>{@code
 * Context withCredential = Context.current().withValues(CRED_KEY, cred);
 * withCredential.wrap(new Runnable() {
 *   public void run() {
 *      readUserRecords(userId, CRED_KEY.get());
 *   }
 * }).run();
 * }</pre>
 *
 * <p>Notes and cautions on use:
 *
 * <ul>
 *   <li>Every {@link #makeCurrent()} must be followed by a {@link Scope#close()}. Breaking these
 *       rules may lead to memory leaks and incorrect scoping.
 *   <li>While Context objects are immutable they do not place such a restriction on the state they
 *       store.
 *   <li>Context is not intended for passing optional parameters to an API and developers should
 *       take care to avoid excessive dependence on context when designing an API.
 *   <li>Attaching Context from a different ancestor will cause information in the current Context
 *       to be lost. This should generally be avoided.
 * </ul>
 */
public interface Context {

  /** Return the context associated with the current {@link Scope}. */
  static Context current() {
    Context current = ContextStorage.get().current();
    return current != null ? current : root();
  }

  /**
   * Returns the root {@link Context} which all other {@link Context} are derived from.
   *
   * <p>It should generally not be required to use the root {@link Context} directly - instead, use
   * {@link Context#current()} to operate on the current {@link Context}. Only use this method if
   * you are absolutely sure you need to disregard the current {@link Context} - this almost always
   * is only a workaround hiding an underlying context propagation issue.
   */
  static Context root() {
    return DefaultContext.ROOT;
  }

  /**
   * Returns the value stored in this {@link Context} for the given {@link ContextKey}, or {@code
   * null} if there is no value for the key in this context.
   */
  @Nullable
  <V> V getValue(ContextKey<V> key);

  /**
   * Returns a new context with the given key value set.
   *
   * <pre>{@code
   * Context withCredential = Context.current().withValues(CRED_KEY, cred);
   * withCredential.wrap(new Runnable() {
   *   public void run() {
   *      readUserRecords(userId, CRED_KEY.get());
   *   }
   * }).run();
   * }</pre>
   *
   * <p>Note that multiple calls to {@code withValue} can be chained together. That is,
   *
   * <pre>{@code
   * context.withValues(K1, V1, K2, V2);
   * // is the same as
   * context.withValue(K1, V1).withValue(K2, V2);
   * }</pre>
   *
   * <p>Nonetheless, {@link Context} should not be treated like a general purpose map with a large
   * number of keys and values — combine multiple related items together into a single key instead
   * of separating them. But if the items are unrelated, have separate keys for them.
   */
  <V> Context withValues(ContextKey<V> k1, V v1);

  /** Returns a new context with the given key value set. */
  default <V1, V2> Context withValues(ContextKey<V1> k1, V1 v1, ContextKey<V2> k2, V2 v2) {
    return withValues(k1, v1).withValues(k2, v2);
  }

  /** Returns a new context with the given key value set. */
  default <V1, V2, V3> Context withValues(
      ContextKey<V1> k1, V1 v1, ContextKey<V2> k2, V2 v2, ContextKey<V3> k3, V3 v3) {
    return withValues(k1, v1, k2, v2).withValues(k3, v3);
  }

  /**
   * Create a new context with the given key value set.
   *
   * <p>For more than 4 key-value pairs, note that multiple calls to {@link #withValues} can be
   * chained together. That is,
   *
   * <pre>
   * context.withValues(K1, V1, K2, V2);
   * // is the same as
   * context.withValue(K1, V1).withValue(K2, V2);
   * </pre>
   *
   * <p>Nonetheless, {@link Context} should not be treated like a general purpose map with a large
   * number of keys and values — combine multiple related items together into a single key instead
   * of separating them. But if the items are unrelated, have separate keys for them.
   */
  default <V1, V2, V3, V4> Context withValues(
      ContextKey<V1> k1,
      V1 v1,
      ContextKey<V2> k2,
      V2 v2,
      ContextKey<V3> k3,
      V3 v3,
      ContextKey<V4> k4,
      V4 v4) {
    return withValues(k1, v1, k2, v2, k3, v3).withValues(k4, v4);
  }

  /**
   * Makes this the {@linkplain Context#current() current context} and returns a {@link Scope} which
   * corresponds to the scope of execution this context is current for. {@link Context#current()}
   * will return this {@link Context} until {@link Scope#close()} is called. {@link Scope#close()}
   * must be called to properly restore the previous context from before this scope of execution or
   * context will not work correctly. It is recommended to use try-with-resources to call {@link
   * Scope#close()} automatically.
   *
   * <pre>{@code
   * Context prevCtx = Context.current();
   * try (Scope ignored = ctx.attach()) {
   *   assert Context.current() == ctx;
   *   ...
   * }
   * assert Context.current() == prevCtx;
   * }</pre>
   */
  default Scope makeCurrent() {
    return ContextStorage.get().attach(this);
  }

  /**
   * Returns a {@link Runnable} that makes this the {@linkplain Context#current current context} and
   * then invokes the input {@link Runnable}.
   */
  default Runnable wrap(Runnable runnable) {
    return () -> {
      try (Scope ignored = makeCurrent()) {
        runnable.run();
      }
    };
  }

  /**
   * Returns a {@link Runnable} that makes this the {@linkplain Context#current current context} and
   * then invokes the input {@link Runnable}.
   */
  default <T> Callable<T> wrap(Callable<T> callable) {
    return () -> {
      try (Scope ignored = makeCurrent()) {
        return callable.call();
      }
    };
  }

  /**
   * Returns an {@link Executor} that will execute callbacks in the given {@code executor}, making
   * this the {@linkplain Context#current current context} before each execution.
   */
  default Executor wrap(Executor executor) {
    return command -> executor.execute(wrap(command));
  }

  /**
   * Returns an {@link ExecutorService} that will execute callbacks in the given {@code executor},
   * making this the {@linkplain Context#current current context} before each execution.
   */
  default ExecutorService wrap(ExecutorService executor) {
    return new ContextExecutorService(this, executor);
  }

  /**
   * Returns an {@link ScheduledExecutorService} that will execute callbacks in the given {@code
   * executor}, making this the {@linkplain Context#current current context} before each execution.
   */
  default ScheduledExecutorService wrap(ScheduledExecutorService executor) {
    return new ContextScheduledExecutorService(this, executor);
  }
}
