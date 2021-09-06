/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.baggage;

import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Builder for {@link BaggageSpanProcessor}.
 *
 * <p>Allows configurable rules for what baggage to include and when to include it.
 */
public class BaggageSpanProcessorBuilder {
  private final SpanProcessor next;
  private Predicate<ReadableSpan> spanFilter = (ignored) -> true;
  private Predicate<String> baggageKeyFilter = (ignored) -> true;

  BaggageSpanProcessorBuilder(SpanProcessor next) {
    this.next = next;
  }

  /**
   * Sets an explicit filter for which Spans will be modified.
   *
   * @param filter A predicate that returns true if a span should be modified.
   */
  public BaggageSpanProcessorBuilder setSpanFilter(Predicate<ReadableSpan> filter) {
    this.spanFilter = filter;
    return this;
  }

  /**
   * Appends key-values from baggage to all spans.
   *
   * <p>Note: This overrides any other baggage filters.
   *
   * @param keyFilter Only baggage key values pairs where the key matches this predicate will be
   *     appended.
   * @return this Builder.
   */
  public BaggageSpanProcessorBuilder filterBaggageAttributes(Predicate<String> keyFilter) {
    this.baggageKeyFilter = keyFilter;
    return this;
  }

  /**
   * Appends key-values from baggage to all spans.
   *
   * <p>Note: This overrides any other baggage filters.
   *
   * @param keyPattern Only baggage key values pairs where the key matches this regex will be
   *     appended.
   * @return this Builder.
   */
  public BaggageSpanProcessorBuilder filterBaggageAttributesByPattern(Pattern keyPattern) {
    this.baggageKeyFilter = StringPredicates.regex(keyPattern);
    return this;
  }

  /** Constructs the {@link BaggageSpanProcessor}. */
  public BaggageSpanProcessor build() {
    return new BaggageSpanProcessor(next, spanFilter, baggageKeyFilter);
  }
}
