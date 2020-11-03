/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Aggregator}. */
class DoubleLastValueAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = DoubleLastValueAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleLastValueAggregator.class);
  }

  @Test
  void toPoint() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    assertNullPoint(aggregator);
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(12.1, offset(1e-6));
    aggregator.recordDouble(13.1);
    aggregator.recordDouble(14.1);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(14.1, offset(1e-6));
  }

  @Test
  void mergeAndReset() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13.1);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(13.1, offset(1e-6));
    Aggregator mergedAggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isCloseTo(13.1, offset(1e-6));
    aggregator.recordDouble(12.1);
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isCloseTo(12.1, offset(1e-6));
  }

  private static DoublePoint getPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get("key")).isEqualTo("value");
    assertThat(point).isInstanceOf(DoublePoint.class);
    return (DoublePoint) point;
  }

  private static void assertNullPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNull();
  }
}
