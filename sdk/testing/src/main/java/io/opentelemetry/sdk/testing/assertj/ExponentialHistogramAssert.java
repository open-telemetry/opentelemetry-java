/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/**
 * Test assertions for {@link ExponentialHistogramData}.
 *
 * @since 1.23.0
 */
public final class ExponentialHistogramAssert
    extends AbstractAssert<ExponentialHistogramAssert, ExponentialHistogramData> {

  ExponentialHistogramAssert(ExponentialHistogramData actual) {
    super(actual, ExponentialHistogramAssert.class);
  }

  /** Ensures that {@code aggregation_temporality} field is {@code CUMULATIVE}. */
  public ExponentialHistogramAssert isCumulative() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.CUMULATIVE) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: CUMULATIVE",
          "Expected Histogram to have cumulative aggregation but found <%s>",
          actual.getAggregationTemporality());
    }
    return this;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code DELTA}. */
  public ExponentialHistogramAssert isDelta() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.DELTA) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: DELTA",
          "Expected Histogram to have cumulative aggregation but found <%s>",
          AggregationTemporality.DELTA,
          actual.getAggregationTemporality());
    }
    return this;
  }

  /**
   * Asserts the exponential histogram has points matching all of the given assertions and no more,
   * in any order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final ExponentialHistogramAssert hasPointsSatisfying(
      Consumer<ExponentialHistogramPointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the exponential histogram has points matching all of the given assertions and no more,
   * in any order.
   */
  public ExponentialHistogramAssert hasPointsSatisfying(
      Iterable<? extends Consumer<ExponentialHistogramPointAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(
            AssertUtil.toConsumers(assertions, ExponentialHistogramPointAssert::new));
    return this;
  }

  /**
   * Asserts that for each given assertion, at least one point in the exponential histogram
   * satisfies it. Extra points that match none of the assertions are allowed, and a single point
   * may satisfy multiple assertions.
   *
   * @since 1.62.0
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final ExponentialHistogramAssert containsPointsSatisfying(
      Consumer<ExponentialHistogramPointAssert>... assertions) {
    return containsPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts that for each given assertion, at least one point in the exponential histogram
   * satisfies it. Extra points that match none of the assertions are allowed, and a single point
   * may satisfy multiple assertions.
   *
   * @since 1.62.0
   */
  public ExponentialHistogramAssert containsPointsSatisfying(
      Iterable<? extends Consumer<ExponentialHistogramPointAssert>> assertions) {
    isNotNull();
    for (Consumer<ExponentialHistogramPointAssert> assertion : assertions) {
      assertThat(actual.getPoints())
          .anySatisfy(point -> assertion.accept(new ExponentialHistogramPointAssert(point)));
    }
    return this;
  }
}
