/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;

public class ExtendedBaggageBuilder {
  private final BaggageBuilder delegate;

  private ExtendedBaggageBuilder(BaggageBuilder delegate) {
    this.delegate = delegate;
  }

  public static ExtendedBaggageBuilder create(BaggageBuilder builder) {
    return new ExtendedBaggageBuilder(builder);
  }

  public static ExtendedBaggageBuilder current() {
    return create(Baggage.current().toBuilder());
  }

  public ExtendedBaggageBuilder set(String key, String value) {
    delegate.put(key, value);
    return this;
  }

  public ExtendedBaggageBuilder setAll(Map<String, String> baggage) {
    baggage.forEach(delegate::put);
    return this;
  }

  /**
   * Set baggage items inside the given {@link SpanCallable}.
   *
   * @param spanCallable the {@link SpanCallable} to call
   * @param <E> the type of the exception
   * @return the result of the {@link SpanCallable}
   */
  public <T, E extends Throwable> T call(SpanCallable<T, E> spanCallable) throws E {
    Context context = delegate.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      return spanCallable.callInSpan();
    }
  }

  public <E extends Throwable> void run(SpanRunnable<E> spanRunnable) throws E {
    Context context = delegate.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      spanRunnable.runInSpan();
    }
  }
}
