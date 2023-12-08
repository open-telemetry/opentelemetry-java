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

public class ExtendedBaggage {
  private final BaggageBuilder builder;

  private ExtendedBaggage(BaggageBuilder builder) {
    this.builder = builder;
  }

  public static ExtendedBaggage create(BaggageBuilder builder) {
    return new ExtendedBaggage(builder);
  }

  public static ExtendedBaggage current() {
    return create(Baggage.current().toBuilder());
  }

  public ExtendedBaggage set(String key, String value) {
    builder.put(key, value);
    return this;
  }

  public ExtendedBaggage setAll(Map<String, String> baggage) {
    baggage.forEach(builder::put);
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
    Context context = builder.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      return spanCallable.callInSpan();
    }
  }

  public <E extends Throwable> void run(SpanRunnable<E> spanRunnable) throws E {
    Context context = builder.build().storeInContext(Context.current());
    try (Scope ignore = context.makeCurrent()) {
      spanRunnable.runInSpan();
    }
  }
}
