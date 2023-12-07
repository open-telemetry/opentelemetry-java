/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;

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
   * Creates a new {@link ExtendedSpanBuilder} with the given span name.
   *
   * @param spanName the name of the span
   * @return the {@link ExtendedSpanBuilder}
   */
  @Override
  public ExtendedSpanBuilder spanBuilder(String spanName) {
    return new ExtendedSpanBuilder(delegate.spanBuilder(spanName));
  }

  /**
   * Set baggage items inside the given {@link SpanCallable}.
   *
   * @param baggage the baggage items to set
   * @param spanCallable the {@link SpanCallable} to call
   * @param <T> the type of the result
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallable}
   */
  @SuppressWarnings("NullAway")
  public static <T, E extends Throwable> T callWithBaggage(
      Map<String, String> baggage, SpanCallable<T, E> spanCallable) throws E {
    BaggageBuilder builder = Baggage.current().toBuilder();
    baggage.forEach(builder::put);
    Context context = builder.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      return spanCallable.callInSpan();
    }
  }
}
