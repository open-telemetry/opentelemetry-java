/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import static io.opentelemetry.api.trace.SpanKind.CONSUMER;
import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.incubator.propagation.Propagation;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Utility class to simplify tracing.
 *
 * <p>The <a
 * href="https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/extended-tracer/README.md">README</a>
 * explains the use cases in more detail.
 */
public final class ExtendedTracer implements Tracer {

  private final Tracer delegate;

  private ExtendedTracer(Tracer delegate) {
    this.delegate = delegate;
  }

  /**
   * Creates a new instance of {@link ExtendedTracer}.
   *
   * @param delegate the {@link Tracer} to use
   */
  public static ExtendedTracer create(Tracer delegate) {
    return new ExtendedTracer(delegate);
  }

  /**
   * Creates a new {@link SpanBuilder} with the given span name.
   *
   * @param spanName the name of the span
   * @return the {@link SpanBuilder}
   */
  @Override
  public SpanBuilder spanBuilder(String spanName) {
    return delegate.spanBuilder(spanName);
  }

  /**
   * Runs the given {@link SpanRunnable} in a new span with the given name and with kind INTERNAL.
   *
   * <p>If an exception is thrown by the {@link SpanRunnable}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param spanName the name of the span
   * @param runnable the {@link SpanRunnable} to run
   */
  public <E extends Throwable> void run(String spanName, SpanRunnable<E> runnable) throws E {
    run(delegate.spanBuilder(spanName), runnable);
  }

  /**
   * Runs the given {@link SpanRunnable} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanRunnable}.
   *
   * <p>If an exception is thrown by the {@link SpanRunnable}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param runnable the {@link SpanRunnable} to run
   */
  @SuppressWarnings("NullAway")
  public <E extends Throwable> void run(SpanBuilder spanBuilder, SpanRunnable<E> runnable)
      throws E {
    call(
        spanBuilder,
        () -> {
          runnable.doInSpan();
          return null;
        });
  }

  /**
   * Runs the given {@link SpanCallback} inside a new span with the given name.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param spanName the name of the span
   * @param spanCallback the {@link SpanCallback} to call
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T call(String spanName, SpanCallback<T, E> spanCallback)
      throws E {
    return call(delegate.spanBuilder(spanName), spanCallback);
  }

  /**
   * Runs the given {@link SpanCallback} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanCallback}.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T call(SpanBuilder spanBuilder, SpanCallback<T, E> spanCallback)
      throws E {
    return call(spanBuilder, spanCallback, ExtendedTracer::setSpanError);
  }

  /**
   * Runs the given {@link SpanCallback} inside of the span created by the given {@link
   * SpanBuilder}. The span will be ended at the end of the {@link SpanCallback}.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the handleException consumer will be
   * called, giving you the opportunity to handle the exception and span in a custom way, e.g. not
   * marking the span as error.
   *
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param handleException the consumer to call when an exception is thrown
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  @SuppressWarnings("NullAway")
  public <T, E extends Throwable> T call(
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      BiConsumer<Span, Throwable> handleException)
      throws E {
    Span span = spanBuilder.startSpan();
    //noinspection unused
    try (Scope unused = span.makeCurrent()) {
      return spanCallback.doInSpan();
    } catch (Throwable e) {
      handleException.accept(span, e);
      throw e;
    } finally {
      span.end();
    }
  }

  /**
   * Marks a span as error.
   *
   * @param span the span
   * @param exception the exception that caused the error
   */
  private static void setSpanError(Span span, Throwable exception) {
    span.setStatus(StatusCode.ERROR);
    span.recordException(exception);
  }

  /**
   * Set baggage items inside the given {@link SpanCallback}.
   *
   * @param baggage the baggage items to set
   * @param spanCallback the {@link SpanCallback} to call
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  @SuppressWarnings("NullAway")
  public static <T, E extends Throwable> T callWithBaggage(
      Map<String, String> baggage, SpanCallback<T, E> spanCallback) throws E {
    BaggageBuilder builder = Baggage.current().toBuilder();
    baggage.forEach(builder::put);
    Context context = builder.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      return spanCallback.doInSpan();
    }
  }

  /**
   * Run the given {@link SpanCallback} inside a server span.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of an event you're processing.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param carrier the string map where to extract the span context from
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T traceServerSpan(
      Map<String, String> carrier,
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      ContextPropagators propagators)
      throws E {
    return extractAndRun(
        SERVER, carrier, spanBuilder, spanCallback, propagators, ExtendedTracer::setSpanError);
  }

  /**
   * Run the given {@link SpanCallback} inside a server span.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of an event you're processing.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the handleException consumer will be
   * called, giving you the opportunity to handle the exception and span in a custom way, e.g. not
   * marking the span as error.
   *
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @param carrier the string map where to extract the span context from
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param handleException the consumer to call when an exception is thrown
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T traceServerSpan(
      Map<String, String> carrier,
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      ContextPropagators propagators,
      BiConsumer<Span, Throwable> handleException)
      throws E {
    return extractAndRun(SERVER, carrier, spanBuilder, spanCallback, propagators, handleException);
  }

  /**
   * Run the given {@link SpanCallback} inside a server span.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of an event you're processing.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the span will be marked as error, and
   * the exception will be recorded.
   *
   * @param carrier the string map where to extract the span context from
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T traceConsumerSpan(
      Map<String, String> carrier,
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      ContextPropagators propagators)
      throws E {
    return extractAndRun(
        CONSUMER, carrier, spanBuilder, spanCallback, propagators, ExtendedTracer::setSpanError);
  }

  /**
   * Run the given {@link SpanRunnable} inside a consumer span.
   *
   * <p>The span context will be extracted from the <code>carrier</code>, which you usually get from
   * HTTP headers of the metadata of an event you're processing.
   *
   * <p>If an exception is thrown by the {@link SpanCallback}, the handleException consumer will be
   * called, giving you the opportunity to handle the exception and span in a custom way, e.g. not
   * marking the span as error.
   *
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @param carrier the string map where to extract the span context from
   * @param spanBuilder the {@link SpanBuilder} to use
   * @param spanCallback the {@link SpanCallback} to call
   * @param propagators provide the propagators from {@link OpenTelemetry#getPropagators()}
   * @param handleException the consumer to call when an exception is thrown
   * @return the result of the {@link SpanCallback}
   */
  public <T, E extends Throwable> T traceConsumerSpan(
      Map<String, String> carrier,
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      ContextPropagators propagators,
      BiConsumer<Span, Throwable> handleException)
      throws E {
    return extractAndRun(
        CONSUMER, carrier, spanBuilder, spanCallback, propagators, handleException);
  }

  private <T, E extends Throwable> T extractAndRun(
      SpanKind spanKind,
      Map<String, String> carrier,
      SpanBuilder spanBuilder,
      SpanCallback<T, E> spanCallback,
      ContextPropagators propagators,
      BiConsumer<Span, Throwable> handleException)
      throws E {
    try (Scope ignore =
        Propagation.extractTextMapPropagationContext(carrier, propagators).makeCurrent()) {
      return call(spanBuilder.setSpanKind(spanKind), spanCallback, handleException);
    }
  }
}
