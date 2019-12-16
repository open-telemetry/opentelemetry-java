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

package io.opentelemetry.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import javax.annotation.concurrent.Immutable;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * <p>Users must interact with the current Context via the public APIs in {@link Tracer} and avoid
 * accessing this class directly.
 *
 * @since 0.1.0
 */
@Immutable
public final class ContextUtils {
  private static final Context.Key<Span> CONTEXT_SPAN_KEY =
      Context.<Span>keyWithDefault("opentelemetry-trace-span-key", DefaultSpan.getInvalid());
  private static final Context.Key<SpanContext> CONTEXT_SPANCONTEXT_KEY =
      Context.<SpanContext>key("opentelemetry-trace-spancontext-key");

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param span the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withSpan(Span span) {
    return withSpan(span, Context.current());
  }

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param span the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withSpan(Span span, Context context) {
    return context.withValue(CONTEXT_SPAN_KEY, span).withValue(CONTEXT_SPANCONTEXT_KEY, null);
  }

  /**
   * Creates a new {@code Context} with the given {@code SpanContext} set.
   *
   * @param spanContext the value to be set.
   * @return a new context with the given value set.
   * @since 0.3.0
   */
  public static Context withSpanContext(SpanContext spanContext) {
    return withSpanContext(spanContext, Context.current());
  }

  /**
   * Creates a new {@code Context} with the given {@code SpanContext} set.
   *
   * @param spanContext the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.3.0
   */
  public static Context withSpanContext(SpanContext spanContext, Context context) {
    return context
        .withValue(CONTEXT_SPAN_KEY, null)
        .withValue(CONTEXT_SPANCONTEXT_KEY, spanContext);
  }

  /**
   * Returns the value from the current {@code Context}.
   *
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static Span getSpan() {
    return CONTEXT_SPAN_KEY.get();
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static Span getSpan(Context context) {
    return CONTEXT_SPAN_KEY.get(context);
  }

  public static SpanContext getSpanContext() {
    return CONTEXT_SPANCONTEXT_KEY.get();
  }

  public static SpanContext getSpanContext(Context context) {
    return CONTEXT_SPANCONTEXT_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@code Span} added to the current {@code
   * Context}.
   *
   * @param span the {@code Span} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withScopedSpan(Span span) {
    return SpanInScope.create(span);
  }

  private ContextUtils() {}
}
