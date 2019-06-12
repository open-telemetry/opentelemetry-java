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

package io.opentelemetry.distributedcontext.unsafe;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.EmptyDistributedContext;

/**
 * Utility methods for accessing the {@link DistributedContext} contained in the {@link
 * io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link
 * DistributedContext} and avoid accessing this class directly.
 *
 * @since 0.1.0
 */
public final class ContextUtils {
  private static final Context.Key<DistributedContext> DIST_CONTEXT_KEY =
      Context.keyWithDefault(
          "opentelemetry-dist-context-key", EmptyDistributedContext.getInstance());

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param distContext the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(DistributedContext distContext) {
    return Context.current().withValue(DIST_CONTEXT_KEY, distContext);
  }

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param distContext the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(DistributedContext distContext, Context context) {
    return context.withValue(DIST_CONTEXT_KEY, distContext);
  }

  /**
   * Returns the value from the current {@code Context}.
   *
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static DistributedContext getValue() {
    return DIST_CONTEXT_KEY.get();
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static DistributedContext getValue(Context context) {
    return DIST_CONTEXT_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@code DistributedContext} added to the
   * current {@code Context}.
   *
   * @param distContext the {@code DistributedContext} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withDistributedContext(DistributedContext distContext) {
    return DistributedContextInScope.create(distContext);
  }

  private ContextUtils() {}
}
