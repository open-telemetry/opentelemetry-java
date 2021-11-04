/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link ExponentialHistogramData}. */
public class ExponentialHistogramAssert
    extends AbstractAssert<ExponentialHistogramAssert, ExponentialHistogramData> {

  protected ExponentialHistogramAssert(ExponentialHistogramData actual) {
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
          "Exepcted Histogram to have cumulative aggregation but found <%s>",
          AggregationTemporality.DELTA,
          actual.getAggregationTemporality());
    }
    return this;
  }

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<
          ?,
          ? extends Iterable<? extends ExponentialHistogramPointData>,
          ExponentialHistogramPointData,
          ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
