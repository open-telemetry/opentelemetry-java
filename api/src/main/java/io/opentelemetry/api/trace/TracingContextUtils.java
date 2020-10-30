/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Util methods/functionality to interact with the {@link Context}. */
@Immutable
final class TracingContextUtils {
  private static final ContextKey<Span> CONTEXT_SPAN_KEY =
      ContextKey.named("opentelemetry-trace-span-key");

  /**
   * Creates a new {@code Context} with the given {@link Span} set.
   *
   * @param span the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   */
  public static Context withSpan(Span span, Context context) {
    return context.with(CONTEXT_SPAN_KEY, span);
  }

  /**
   * Returns the {@link Span} from the current {@code Context}, falling back to a default, no-op
   * {@link Span}.
   *
   * @return the {@link Span} from the current {@code Context}.
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
   */
  public static Span getSpan(Context context) {
    Span span = context.get(CONTEXT_SPAN_KEY);
    return span == null ? Span.getInvalid() : span;
  }

  /**
   * Returns the {@link Span} from the specified {@code Context}. If none is found, this method
   * returns {code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Span} from the specified {@code Context}.
   */
  @Nullable
  public static Span getSpanWithoutDefault(Context context) {
    return context.get(CONTEXT_SPAN_KEY);
  }

  private TracingContextUtils() {}
}
