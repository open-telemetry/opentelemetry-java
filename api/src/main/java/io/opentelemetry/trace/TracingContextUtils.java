/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Util methods/functionality to interact with the {@link Context}. */
@Immutable
public final class TracingContextUtils {
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
    return context.withValues(CONTEXT_SPAN_KEY, span);
  }

  /**
   * Returns the {@link Span} from the current {@code Context}, falling back to a default, no-op
   * {@link Span}.
   *
   * @return the {@link Span} from the current {@code Context}.
   */
  public static Span getCurrentSpan() {
    return getSpan(io.opentelemetry.context.Context.current());
  }

  /**
   * Returns the {@link Span} from the specified {@code Context}, falling back to a default, no-op
   * {@link Span}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link Span} from the specified {@code Context}.
   */
  public static Span getSpan(Context context) {
    Span span = context.getValue(CONTEXT_SPAN_KEY);
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
    return context.getValue(CONTEXT_SPAN_KEY);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@link Span} added to the current {@code
   * Context}.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * private static Tracer tracer = OpenTelemetry.getTracer();
   * void doWork() {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.spanBuilder("my span").startSpan();
   *   try (Scope ws = TracingContextUtils.currentContextWith(span)) {
   *     TracingContextUtils.getCurrentSpan().addEvent("my event");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   }
   *   span.end();
   * }
   * }</pre>
   *
   * @param span the {@link Span} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   */
  @MustBeClosed
  public static Scope currentContextWith(Span span) {
    return withSpan(span, io.opentelemetry.context.Context.current()).makeCurrent();
  }

  private TracingContextUtils() {}
}
