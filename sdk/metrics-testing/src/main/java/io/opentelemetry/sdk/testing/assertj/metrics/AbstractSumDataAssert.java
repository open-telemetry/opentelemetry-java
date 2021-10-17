/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.SumData;
import org.assertj.core.api.AbstractAssert;

/** Test assertions for {@link SumData}. */
public class AbstractSumDataAssert<
        SumAssertT extends AbstractSumDataAssert<SumAssertT, SumT>, SumT extends SumData<?>>
    extends AbstractAssert<SumAssertT, SumT> {
  protected AbstractSumDataAssert(SumT actual, Class<SumAssertT> assertClass) {
    super(actual, assertClass);
  }

  /** Ensures that {@code is_monotonic} field is true. */
  public SumAssertT isMonotonic() {
    isNotNull();
    if (!actual.isMonotonic()) {
      failWithActualExpectedAndMessage(
          actual, "montonic: true", "Exepcted Sum to be monotonic", true, actual.isMonotonic());
    }
    return myself;
  }

  /** Ensures that {@code is_monotonic} field is false. */
  public SumAssertT isNotMonotonic() {
    isNotNull();
    if (actual.isMonotonic()) {
      failWithActualExpectedAndMessage(
          actual,
          "montonic: fail",
          "Exepcted Sum to be non-monotonic, found: %s",
          actual.isMonotonic());
    }
    return myself;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code CUMULATIVE}. */
  public SumAssertT isCumulative() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.CUMULATIVE) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: CUMULATIVE",
          "Exepcted Sum to have cumulative aggregation but found <%s>",
          actual.getAggregationTemporality());
    }
    return myself;
  }

  /** Ensures that {@code aggregation_temporality} field is {@code DELTA}. */
  public SumAssertT isDelta() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.DELTA) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: DELTA",
          "Exepected Sum to have delta aggregation but found <%s>",
          actual.getAggregationTemporality());
    }
    return myself;
  }
}
