/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/** Extended {@link SpanBuilder} with experimental APIs. */
public interface ExtendedSpanBuilder extends SpanBuilder {

  /**
   * Extract a span context from the given carrier and set it as parent of the span for {@link
   * #startAndCall(SpanCallable)} and {@link #startAndRun(SpanRunnable)}.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of a message you're processing.
   *
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param carrier the string map where to extract the span context from
   */
  ExtendedSpanBuilder setParentFrom(ContextPropagators propagators, Map<String, String> carrier);

  /**
   * Runs the given {@link SpanCallable} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanCallable}.
   *
   * <p>If an exception is thrown by the {@link SpanCallable}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param spanCallable the {@link SpanCallable} to call
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallable}
   */
  <T, E extends Throwable> T startAndCall(SpanCallable<T, E> spanCallable) throws E;

  /**
   * Runs the given {@link SpanCallable} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanCallable}.
   *
   * <p>If an exception is thrown by the {@link SpanCallable}, the <code>handleException</code>
   * consumer will be called, giving you the opportunity to handle the exception and span in a
   * custom way, e.g. not marking the span as error.
   *
   * @param spanCallable the {@link SpanCallable} to call
   * @param handleException the consumer to call when an exception is thrown
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallable}
   */
  <T, E extends Throwable> T startAndCall(
      SpanCallable<T, E> spanCallable, BiConsumer<Span, Throwable> handleException) throws E;

  /**
   * Runs the given {@link SpanRunnable} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanRunnable}.
   *
   * <p>If an exception is thrown by the {@link SpanRunnable}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param runnable the {@link SpanRunnable} to run
   * @param <E> the type of the exception
   */
  <E extends Throwable> void startAndRun(SpanRunnable<E> runnable) throws E;

  /**
   * Runs the given {@link SpanRunnable} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanRunnable}.
   *
   * <p>If an exception is thrown by the {@link SpanRunnable}, the <code>handleException</code>
   * consumer will be called, giving you the opportunity to handle the exception and span in a
   * custom way, e.g. not marking the span as error.
   *
   * @param runnable the {@link SpanRunnable} to run
   * @param <E> the type of the exception
   */
  <E extends Throwable> void startAndRun(
      SpanRunnable<E> runnable, BiConsumer<Span, Throwable> handleException) throws E;

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setParent(Context context);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setNoParent();

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder addLink(SpanContext spanContext);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder addLink(SpanContext spanContext, Attributes attributes);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setAttribute(String key, @Nullable String value);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setAttribute(String key, long value);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setAttribute(String key, double value);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setAttribute(String key, boolean value);

  /** {@inheritDoc} */
  @Override
  <T> ExtendedSpanBuilder setAttribute(AttributeKey<T> key, @Nullable T value);

  /** {@inheritDoc} */
  @Override
  default ExtendedSpanBuilder setAllAttributes(Attributes attributes) {
    return (ExtendedSpanBuilder) SpanBuilder.super.setAllAttributes(attributes);
  }

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setSpanKind(SpanKind spanKind);

  /** {@inheritDoc} */
  @Override
  ExtendedSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit);

  /** {@inheritDoc} */
  @Override
  default ExtendedSpanBuilder setStartTimestamp(Instant startTimestamp) {
    return (ExtendedSpanBuilder) SpanBuilder.super.setStartTimestamp(startTimestamp);
  }
}
