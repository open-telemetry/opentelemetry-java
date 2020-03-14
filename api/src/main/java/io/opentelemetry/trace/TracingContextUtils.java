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
public final class TracingContextUtils {
  private static final Context.Key<Span> CONTEXT_SPAN_KEY =
      Context.<Span>keyWithDefault("opentelemetry-trace-span-key", DefaultSpan.getInvalid());
  private static final Context.Key<SpanContext> CONTEXT_SPANCONTEXT_KEY =
      Context.<SpanContext>keyWithDefault(
          "opentelemetry-trace-spancontext-key", SpanContext.getInvalid());

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
   * Creates a new {@code Context} with the given {@link SpanContext} set.
   *
   * @param spanContext the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.3.0
   */
  public static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.withValue(CONTEXT_SPANCONTEXT_KEY, spanContext);
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
    return CONTEXT_SPAN_KEY.get(context);
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
    Span span = CONTEXT_SPAN_KEY.get(context);
    return span == DefaultSpan.getInvalid() ? null : span;
  }

  /**
   * Returns the {@link SpanContext} from the specified {@code Context}, falling back to a default,
   * no-op {@link SpanContext}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link SpanContext} from the specified {@code Context}.
   * @since 0.3.0
   */
  public static SpanContext getSpanContext(Context context) {
    return CONTEXT_SPANCONTEXT_KEY.get(context);
  }

  /**
   * Returns the {@link SpanContext} from the specified {@code Context}. If none is found, this
   * method returns {@code null}.
   *
   * @param context the specified {@code Context}.
   * @return the {@link SpanContext} from the specified {@code Context}.
   * @since 0.3.0
   */
  @Nullable
  public static SpanContext getSpanContextWithoutDefault(Context context) {
    SpanContext spanContext = CONTEXT_SPANCONTEXT_KEY.get(context);
    return spanContext == SpanContext.getInvalid() ? null : spanContext;
  }

  /**
   * Returns the effective {@link SpanContext} from the specified {@code Context}.
   *
   * <p>This method tries to get any effective non-null {@link SpanContext} in {@code Context},
   * giving higher priority to {@link Span#getContext()} and then falling back to {@link
   * SpanContext}. If none is found, this method returns {@code null}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.3.0
   */
  @Nullable
  public static SpanContext getEffectiveSpanContext(Context context) {
    Span span = getSpanWithoutDefault(context);
    return span != null ? span.getContext() : getSpanContextWithoutDefault(context);
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
