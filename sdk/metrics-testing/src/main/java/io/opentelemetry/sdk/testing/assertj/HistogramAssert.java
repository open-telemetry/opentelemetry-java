/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link HistogramData}. */
public class HistogramAssert extends AbstractAssert<HistogramAssert, HistogramData> {

  protected HistogramAssert(HistogramData actual) {
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

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends HistogramPointData>, HistogramPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
