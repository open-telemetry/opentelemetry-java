/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.assertj.core.api.AbstractIterableAssert;

/** Assertions for a list of exported traces. */
public final class TracesAssert
    extends AbstractIterableAssert<
        TracesAssert, List<List<SpanData>>, List<SpanData>, TraceAssert> {

  /** Compare spans by start time, placing parents before their children as a tiebreaker. */
  static final Comparator<SpanData> SPAN_DATA_COMPARATOR =
      Comparator.comparing(SpanData::getStartEpochNanos)
          .thenComparing(
              (span1, span2) -> {
                SpanContext parent1 = span1.getParentSpanContext();
                if (parent1.isValid() && parent1.getSpanId().equals(span2.getSpanId())) {
                  return 1;
                }
                SpanContext parent2 = span2.getParentSpanContext();
                if (parent2.isValid() && parent2.getSpanId().equals(span1.getSpanId())) {
                  return -1;
                }
                return 0;
              });

  /**
   * Returns an assertion for a list of traces. The provided spans will be grouped into traces by
   * their trace ID.
   *
   * @since 1.23.0
   */
  public static TracesAssert assertThat(List<SpanData> spanData) {
    Map<String, List<SpanData>> traces =
        spanData.stream()
            .collect(
                Collectors.groupingBy(
                    SpanData::getTraceId,
                    LinkedHashMap::new,
                    Collectors.toCollection(ArrayList::new)));
    for (List<SpanData> trace : traces.values()) {
      trace.sort(SPAN_DATA_COMPARATOR);
    }
    return assertThat(traces.values());
  }

  /**
   * Returns an assertion for a list of traces. The traces must already be grouped into {@code
   * List<SpanData>} where each list has spans with the same trace ID.
   */
  public static TracesAssert assertThat(Collection<List<SpanData>> traces) {
    for (List<SpanData> trace : traces) {
      if (trace.stream().map(SpanData::getTraceId).distinct().count() != 1) {
        throw new IllegalArgumentException(
            "trace does not have consistent trace IDs, group spans into traces before calling "
                + "this function: "
                + trace);
      }
    }
    return new TracesAssert(new ArrayList<>(traces));
  }

  TracesAssert(List<List<SpanData>> lists) {
    super(lists, TracesAssert.class);
  }

  /**
   * Asserts that the traces under assertion have the same number of traces as provided {@code
   * assertions} and executes each {@link TracesAssert} in {@code assertions} in order with the
   * corresponding trace.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final TracesAssert hasTracesSatisfyingExactly(Consumer<TraceAssert>... assertions) {
    return hasTracesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts that the traces under assertion have the same number of traces as provided {@code
   * assertions} and executes each {@link TracesAssert} in {@code assertions} in order with the
   * corresponding trace.
   */
  public TracesAssert hasTracesSatisfyingExactly(
      Iterable<? extends Consumer<TraceAssert>> assertions) {
    List<Consumer<TraceAssert>> assertionsList =
        StreamSupport.stream(assertions.spliterator(), false).collect(toList());
    hasSize(assertionsList.size());
    // Avoid zipSatisfy - https://github.com/assertj/assertj-core/issues/2300
    for (int i = 0; i < assertionsList.size(); i++) {
      assertionsList.get(i).accept(new TraceAssert(actual.get(i)));
    }
    return this;
  }

  @Override
  protected TraceAssert toAssert(List<SpanData> value, String description) {
    return new TraceAssert(value).as(description);
  }

  @Override
  protected TracesAssert newAbstractIterableAssert(Iterable<? extends List<SpanData>> iterable) {
    return new TracesAssert(StreamSupport.stream(iterable.spliterator(), false).collect(toList()));
  }
}
