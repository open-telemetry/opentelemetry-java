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

package io.opentelemetry.correlationcontext;

import io.grpc.Context;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Utility methods for accessing the {@link CorrelationContext} contained in the {@link
 * io.grpc.Context}.
 *
 * @since 0.1.0
 */
@Immutable
public final class CorrelationsContextUtils {
  private static final Context.Key<CorrelationContext> CORR_CONTEXT_KEY =
      Context.key("opentelemetry-corr-context-key");

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
   * Returns the {@link CorrelationContext} from the current {@code Context}, falling back to an
   * empty {@link CorrelationContext}.
   *
   * @return the {@link CorrelationContext} from the current {@code Context}.
   * @since 0.3.0
   */
  public static CorrelationContext getCurrentCorrelationContext() {
    return getCorrelationContext(Context.current());
  }

  /**
   * Returns the {@link CorrelationContext} from the specified {@code Context}, falling back to an
   * empty {@link CorrelationContext}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link CorrelationContext} from the specified {@code Context}.
   * @since 0.3.0
   */
  public static CorrelationContext getCorrelationContext(Context context) {
    CorrelationContext corrContext = CORR_CONTEXT_KEY.get(context);
    return corrContext == null ? EmptyCorrelationContext.getInstance() : corrContext;
  }

  /**
   * Returns the {@link CorrelationContext} from the specified {@code Context}. If none is found,
   * this method returns {code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link CorrelationContext} from the specified {@code Context}.
   * @since 0.1.0
   */
  @Nullable
  public static CorrelationContext getCorrelationContextWithoutDefault(Context context) {
    return CORR_CONTEXT_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@link CorrelationContext} added to the
   * current {@code Context}.
   *
   * @param corrContext the {@link CorrelationContext} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope currentContextWith(CorrelationContext corrContext) {
    Context context = withCorrelationContext(corrContext, Context.current());
    return ContextUtils.withScopedContext(context);
  }

  private CorrelationsContextUtils() {}
}
