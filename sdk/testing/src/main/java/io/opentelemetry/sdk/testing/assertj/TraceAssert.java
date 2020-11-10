/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.assertj.core.api.AbstractIterableAssert;

/** Assertions for an exported trace, a list of {@link SpanData} with the same trace ID. */
public class TraceAssert
    extends AbstractIterableAssert<TraceAssert, List<SpanData>, SpanData, SpanDataAssert> {

  TraceAssert(List<SpanData> spanData) {
    super(spanData, TraceAssert.class);
  }

  /** Asserts that the trace has the given trace ID. */
  public TraceAssert hasTraceId(String traceId) {
    isNotNull();
    isNotEmpty();

    String actualTraceId = actual.get(0).getTraceId();
    if (!actualTraceId.equals(traceId)) {
      failWithActualExpectedAndMessage(
          actualTraceId,
          traceId,
          "Expected trace to have trace ID <%s> but was <%s>",
          traceId,
          actualTraceId);
    }
    return this;
  }

  /**
   * Asserts that the trace under assertion has the same number of spans as provided {@code
   * assertions} and executes each {@link SpanDataAssert} in {@code assertions} in order with the
   * corresponding span.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final TraceAssert hasSpansSatisfyingExactly(Consumer<SpanDataAssert>... assertions) {
    hasSize(assertions.length);
    zipSatisfy(
        Arrays.asList(assertions), (span, assertion) -> assertion.accept(new SpanDataAssert(span)));
    return this;
  }

  @Override
  protected SpanDataAssert toAssert(SpanData value, String description) {
    return new SpanDataAssert(value).as(description);
  }

  @Override
  protected TraceAssert newAbstractIterableAssert(Iterable<? extends SpanData> iterable) {
    return new TraceAssert(
        StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList()));
  }
}
