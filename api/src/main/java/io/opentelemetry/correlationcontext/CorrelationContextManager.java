/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.correlationcontext;

import io.opentelemetry.context.Scope;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Object for creating new {@link CorrelationContext}s and {@code CorrelationContext}s based on the
 * current context.
 *
 * <p>This class returns {@link CorrelationContext.Builder builders} that can be used to create the
 * implementation-dependent {@link CorrelationContext}s.
 *
 * <p>Implementations may have different constraints and are free to convert entry contexts to their
 * own subtypes. This means callers cannot assume the {@link #getCurrentContext() current context}
 * is the same instance as the one {@link #withContext(CorrelationContext) placed into scope}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface CorrelationContextManager {

  /**
   * Returns the current {@code CorrelationContext}.
   *
   * @return the current {@code CorrelationContext}.
   * @since 0.1.0
   */
  CorrelationContext getCurrentContext();

  /**
   * Returns a new {@code Builder}.
   *
   * @return a new {@code Builder}.
   * @since 0.1.0
   */
  CorrelationContext.Builder contextBuilder();

  /**
   * Enters the scope of code where the given {@code CorrelationContext} is in the current context
   * (replacing the previous {@code CorrelationContext}) and returns an object that represents that
   * scope. The scope is exited when the returned object is closed.
   *
   * @param distContext the {@code CorrelationContext} to be set as the current context.
   * @return an object that defines a scope where the given {@code CorrelationContext} is set as the
   *     current context.
   * @since 0.1.0
   */
  Scope withContext(CorrelationContext distContext);
}
