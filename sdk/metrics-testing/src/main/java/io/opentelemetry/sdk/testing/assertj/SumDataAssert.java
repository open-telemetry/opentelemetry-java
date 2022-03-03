/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link SumData}. */
public class SumDataAssert<T extends PointData>
    extends AbstractAssert<SumDataAssert<T>, SumData<T>> {
  protected SumDataAssert(SumData<T> actual) {
    super(actual, SumDataAssert.class);
  }

  /** Ensures that {@code is_monotonic} field is true. */
  public SumDataAssert<T> isMonotonic() {
    isNotNull();
    if (!actual.isMonotonic()) {
      failWithActualExpectedAndMessage(
          actual, "monotonic: true", "Expected Sum to be monotonic", true, actual.isMonotonic());
    }
    return myself;
  }

  /** Ensures that {@code is_monotonic} field is false. */
  public SumDataAssert<T> isNotMonotonic() {
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
  public SumDataAssert<T> isCumulative() {
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
  public SumDataAssert<T> isDelta() {
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

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<?, ? extends Iterable<? extends T>, T, ?> points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
