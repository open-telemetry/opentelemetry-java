/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.correlationcontext.propagation;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextUtils;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.EmptyCorrelationContext;
import javax.annotation.concurrent.Immutable;

/**
 * Utility methods for accessing the {@link CorrelationContext} contained in the {@link
 * io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link
 * CorrelationContext} and avoid accessing this class directly.
 *
 * @since 0.1.0
 */
@Immutable
public final class CorrelationsContextUtils {
  private static final Context.Key<CorrelationContext> CORR_CONTEXT_KEY =
      Context.key("opentelemetry-corr-context-key");
  private static final CorrelationContext DEFAULT_VALUE = EmptyCorrelationContext.getInstance();

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param corrContext the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withCorrelationContext(CorrelationContext corrContext) {
    return withCorrelationContext(corrContext, Context.current());
  }

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param corrContext the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withCorrelationContext(CorrelationContext corrContext, Context context) {
    return context.withValue(CORR_CONTEXT_KEY, corrContext);
  }

  /**
   * Returns the value from the current {@code Context}.
   *
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static CorrelationContext getCorrelationContext() {
    return CORR_CONTEXT_KEY.get();
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static CorrelationContext getCorrelationContext(Context context) {
    return CORR_CONTEXT_KEY.get(context);
  }

  /**
   * Returns the value from the specified {@code Context}, falling back to a default, no-op {@link
   * CorrelationContext}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.3.0
   */
  public static CorrelationContext getCorrelationContextWithDefault(Context context) {
    CorrelationContext corrContext = CORR_CONTEXT_KEY.get(context);
    return corrContext == null ? DEFAULT_VALUE : corrContext;
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@code CorrelationContext} added to the
   * current {@code Context}.
   *
   * @param corrContext the {@code CorrelationContext} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withScopedCorrelationContext(CorrelationContext corrContext) {
    Context context = withCorrelationContext(corrContext);
    return ContextUtils.withScopedContext(context);
  }

  private CorrelationsContextUtils() {}
}
