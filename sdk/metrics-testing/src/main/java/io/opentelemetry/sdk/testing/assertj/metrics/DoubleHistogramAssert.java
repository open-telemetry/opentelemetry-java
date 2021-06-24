/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

public class DoubleHistogramAssert
    extends AbstractAssert<DoubleHistogramAssert, DoubleHistogramData> {

  protected DoubleHistogramAssert(DoubleHistogramData actual) {
    super(actual, DoubleHistogramAssert.class);
  }

  public DoubleHistogramAssert isCumulative() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.CUMULATIVE) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: CUMULATIVE",
          "Exepcted Histgram to have cumulative aggregation but found <%s>",
          AggregationTemporality.CUMULATIVE,
          actual.getAggregationTemporality());
    }
    return this;
  }

  public DoubleHistogramAssert isDelta() {
    isNotNull();
    if (actual.getAggregationTemporality() != AggregationTemporality.DELTA) {
      failWithActualExpectedAndMessage(
          actual,
          "aggregationTemporality: DELTA",
          "Exepcted Histgram to have cumulative aggregation but found <%s>",
          AggregationTemporality.DELTA,
          actual.getAggregationTemporality());
    }
    return this;
  }

  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends DoubleHistogramPointData>, DoubleHistogramPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
