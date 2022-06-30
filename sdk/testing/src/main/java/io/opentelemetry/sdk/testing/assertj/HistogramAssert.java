/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/**
 * Test assertions for {@link HistogramData}.
 *
 * @since 1.14.0
 */
public final class HistogramAssert extends AbstractAssert<HistogramAssert, HistogramData> {

  HistogramAssert(HistogramData actual) {
    super(actual, HistogramAssert.class);
  }

  /** Ensures that {@code aggregation_temporality} field is {@code CUMULATIVE}. */
  public HistogramAssert isCumulative() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.CUMULATIVE) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: CUMULATIVE",
          "Expected Histogram to have cumulative aggregation but found <%s>",
          AggregationTemporality.CUMULATIVE,
          actual.getAggregationTemporality());
    }
    return this;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code DELTA}. */
  public HistogramAssert isDelta() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.DELTA) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: DELTA",
          "Expected Histgram to have cumulative aggregation but found <%s>",
          AggregationTemporality.DELTA,
          actual.getAggregationTemporality());
    }
    return this;
  }

  /**
   * Asserts the histogram has points matching all of the given assertions and no more, in any
   * order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final HistogramAssert hasPointsSatisfying(Consumer<HistogramPointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the histogram has points matching all of the given assertions and no more, in any
   * order.
   */
  public HistogramAssert hasPointsSatisfying(
      Iterable<? extends Consumer<HistogramPointAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, HistogramPointAssert::new));
    return this;
  }
}
