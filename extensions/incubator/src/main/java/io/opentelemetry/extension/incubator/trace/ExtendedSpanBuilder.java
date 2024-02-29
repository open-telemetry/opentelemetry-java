/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.incubator.propagation.ExtendedContextPropagators;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public final class ExtendedSpanBuilder implements SpanBuilder {
  private final SpanBuilder delegate;

  ExtendedSpanBuilder(SpanBuilder delegate) {
    this.delegate = delegate;
  }

  @Override
  public ExtendedSpanBuilder setParent(Context context) {
    delegate.setParent(context);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setNoParent() {
    delegate.setNoParent();
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext) {
    delegate.addLink(spanContext);
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
    delegate.addLink(spanContext, attributes);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, String value) {
    delegate.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, long value) {
    delegate.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, double value) {
    delegate.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, boolean value) {
    delegate.setAttribute(key, value);
    return this;
  }

  @Override
  public <T> ExtendedSpanBuilder setAttribute(AttributeKey<T> key, T value) {
    delegate.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAllAttributes(Attributes attributes) {
    delegate.setAllAttributes(attributes);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setSpanKind(SpanKind spanKind) {
    delegate.setSpanKind(spanKind);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
    delegate.setStartTimestamp(startTimestamp, unit);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setStartTimestamp(Instant startTimestamp) {
    delegate.setStartTimestamp(startTimestamp);
    return this;
  }

  /**
   * Extract a span context from the given carrier and set it as parent of the span for {@link
   * #startAndCall(SpanCallable)} and {@link #startAndRun(SpanRunnable)}.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of a message you're processing.
   *
   * <p>A typical usage would be: <code>
   * ExtendedTracer.create(tracer)
   * .setSpanKind(SpanKind.SERVER)
   * .setParentFrom(propagators, carrier)
   * .run("my-span", () -> { ... });
   * </code>
   *
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param carrier the string map where to extract the span context from
   */
  public ExtendedSpanBuilder setParentFrom(
      ContextPropagators propagators, Map<String, String> carrier) {
    setParent(ExtendedContextPropagators.extractTextMapPropagationContext(carrier, propagators));
    return this;
  }

  @Override
  public Span startSpan() {
    return delegate.startSpan();
  }

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
  public <T, E extends Throwable> T startAndCall(SpanCallable<T, E> spanCallable) throws E {
    return startAndCall(spanCallable, ExtendedSpanBuilder::setSpanError);
  }

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
  public <T, E extends Throwable> T startAndCall(
      SpanCallable<T, E> spanCallable, BiConsumer<Span, Throwable> handleException) throws E {
    Span span = startSpan();

    //noinspection unused
    try (Scope unused = span.makeCurrent()) {
      return spanCallable.callInSpan();
    } catch (Throwable e) {
      handleException.accept(span, e);
      throw e;
    } finally {
      span.end();
    }
  }

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
  @SuppressWarnings("NullAway")
  public <E extends Throwable> void startAndRun(SpanRunnable<E> runnable) throws E {
    startAndRun(runnable, ExtendedSpanBuilder::setSpanError);
  }

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
  @SuppressWarnings("NullAway")
  public <E extends Throwable> void startAndRun(
      SpanRunnable<E> runnable, BiConsumer<Span, Throwable> handleException) throws E {
    startAndCall(
        () -> {
          runnable.runInSpan();
          return null;
        },
        handleException);
  }

  /**
   * Marks a span as error. This is the default exception handler.
   *
   * @param span the span
   * @param exception the exception that caused the error
   */
  private static void setSpanError(Span span, Throwable exception) {
    span.setStatus(StatusCode.ERROR);
    span.recordException(exception);
  }
}
