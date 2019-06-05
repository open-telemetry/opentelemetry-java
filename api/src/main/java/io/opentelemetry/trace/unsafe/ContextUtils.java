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

package io.opentelemetry.trace.unsafe;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * <p>Users must interact with the current Context via the public APIs in {@link Tracer} and avoid
 * accessing this class directly.
 *
 * @since 0.1.0
 */
public final class ContextUtils {
  private static final Context.Key<Span> CONTEXT_SPAN_KEY =
      Context.keyWithDefault("opentelemetry-trace-span-key", DefaultSpan.INSTANCE);

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param span the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(Span span) {
    return Context.current().withValue(CONTEXT_SPAN_KEY, span);
  }

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param span the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(Span span, Context context) {
    return context.withValue(CONTEXT_SPAN_KEY, span);
  }

  /**
   * Returns the value from the current {@code Context}.
   *
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static Span getValue() {
    return CONTEXT_SPAN_KEY.get();
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static Span getValue(Context context) {
    return CONTEXT_SPAN_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@code Span} added to the current {@code
   * Context}.
   *
   * @param span the {@code Span} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withSpan(Span span) {
    return SpanInScope.create(span);
  }

  private ContextUtils() {}
}
