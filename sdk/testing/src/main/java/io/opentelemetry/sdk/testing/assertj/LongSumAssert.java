/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/**
 * Test assertions for long {@link SumData}.
 *
 * @since 1.14.0
 */
public final class LongSumAssert extends AbstractAssert<LongSumAssert, SumData<LongPointData>> {
  LongSumAssert(SumData<LongPointData> actual) {
    super(actual, LongSumAssert.class);
  }

  /** Ensures that {@code is_monotonic} field is true. */
  public LongSumAssert isMonotonic() {
    isNotNull();
    if (!actual.isMonotonic()) {
      failWithActualExpectedAndMessage(
          actual, "monotonic: true", "Expected Sum to be monotonic", true, actual.isMonotonic());
    }
    return myself;
  }

  /** Ensures that {@code is_monotonic} field is false. */
  public LongSumAssert isNotMonotonic() {
    isNotNull();
    if (actual.isMonotonic()) {
      failWithActualExpectedAndMessage(
          actual,
          "monotonic: fail",
          "Expected Sum to be non-monotonic, found: %s",
          actual.isMonotonic());
    }
    return myself;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code CUMULATIVE}. */
  public LongSumAssert isCumulative() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.CUMULATIVE) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: CUMULATIVE",
          "Expected Sum to have cumulative aggregation but found <%s>",
          actual.getAggregationTemporality());
    }
    return myself;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code DELTA}. */
  public LongSumAssert isDelta() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.DELTA) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: DELTA",
          "Expected Sum to have delta aggregation but found <%s>",
          actual.getAggregationTemporality());
    }
    return myself;
  }

  /** Asserts the sum has points matching all of the given assertions and no more, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final LongSumAssert hasPointsSatisfying(Consumer<LongPointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the sum has points matching all of the given assertions and no more, in any order. */
  public LongSumAssert hasPointsSatisfying(
      Iterable<? extends Consumer<LongPointAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, LongPointAssert::new));
    return this;
  }
}
