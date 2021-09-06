/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.baggage;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.function.Predicate;

/** Span processor which appends {@link Baggage} on start before passing to next processor. */
public class BaggageSpanProcessor implements SpanProcessor {
  private final Predicate<ReadableSpan> spanFilter;
  private final Predicate<String> baggageKeyFilter;
  private final SpanProcessor next;

  /**
   * Constructs a new baggage span processor that appends baggage to a span.
   *
   * @param next The next processor in the chain.
   */
  BaggageSpanProcessor(
      SpanProcessor next, Predicate<ReadableSpan> spanFilter, Predicate<String> baggageKeyFilter) {
    this.next = next;
    this.spanFilter = spanFilter;
    this.baggageKeyFilter = baggageKeyFilter;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    Baggage baggage = Baggage.fromContext(parentContext);
    if (!baggage.isEmpty() && spanFilter.test(span)) {
      // TODO: append-only rather than overwrite with baggage.
      baggage.forEach(
          (k, v) -> {
            if (baggageKeyFilter.test(k)) {
              span.setAttribute(k, v.getValue());
            }
          });
    }
    next.onStart(parentContext, span);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    next.onEnd(span);
  }

  @Override
  public boolean isEndRequired() {
    return next.isEndRequired();
  }

  /**
   * Returns a builder for a baggage span processor.
   *
   * @param next The next processor in the chain.
   */
  public static final BaggageSpanProcessorBuilder builder(SpanProcessor next) {
    return new BaggageSpanProcessorBuilder(next);
  }
}
