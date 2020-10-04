/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.grpc.Context;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * @since 0.1.0
 */
@Immutable
final class TracingContextUtils {
  private static final Context.Key<Span> CONTEXT_SPAN_KEY =
      Context.key("opentelemetry-trace-span-key");

  /**
   * Creates a new {@code Context} with the given {@link Span} set.
   *
   * @param span the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withSpan(Span span, Context context) {
    return context.withValue(CONTEXT_SPAN_KEY, span);
  }

  /**
   * Returns the {@link Span} from the current {@code Context}, falling back to a default, no-op
   * {@link Span}.
   *
   * @return the {@link Span} from the current {@code Context}.
   * @since 0.3.0
   */
  public static Span getCurrentSpan() {
    return getSpan(Context.current());
  }

  /**
   * Returns the {@link Span} from the specified {@code Context}, falling back to a default, no-op
   * {@link Span}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Span} from the specified {@code Context}.
   * @since 0.3.0
   */
  public static Span getSpan(Context context) {
    Span span = CONTEXT_SPAN_KEY.get(context);
    return span == null ? DefaultSpan.getInvalid() : span;
  }

  /**
   * Returns the {@link Span} from the specified {@code Context}. If none is found, this method
   * returns {code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Span} from the specified {@code Context}.
   * @since 0.1.0
   */
  @Nullable
  public static Span getSpanWithoutDefault(Context context) {
    return CONTEXT_SPAN_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@link Span} added to the current {@code
   * Context}.
   *
   * @param span the {@link Span} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope currentContextWith(Span span) {
    return ContextUtils.withScopedContext(withSpan(span, Context.current()));
  }

  private TracingContextUtils() {}
}
