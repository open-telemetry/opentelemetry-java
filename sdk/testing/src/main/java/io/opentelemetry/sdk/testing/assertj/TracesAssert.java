/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import org.assertj.core.api.AbstractIterableAssert;

/** Assertions for a list of exported traces. */
public class TracesAssert
    extends AbstractIterableAssert<
        TracesAssert, List<List<SpanData>>, List<SpanData>, TraceAssert> {

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
  public final TracesAssert hasTracesSatisfyingInAnyOrder(Consumer<TraceAssert>... assertions) {
    hasSize(assertions.length);
    zipSatisfy(
        Arrays.asList(assertions), (trace, assertion) -> assertion.accept(new TraceAssert(trace)));
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
