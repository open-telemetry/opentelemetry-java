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

/** Unit tests for {@link DoubleSumAggregator}. */
class DoubleSumAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = DoubleSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleSumAggregator.class);
  }

  @Test
  void toPoint() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    assertThat(getPoint(aggregator).getValue()).isCloseTo(0, offset(1e-6));
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(12.1 * 5, offset(1e-6));
  }

  @Test
  void multipleRecords_WithNegatives() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(-23.2);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.3);
    aggregator.recordDouble(-11.3);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(14.1, offset(1e-6));
  }

  @Test
  void mergeAndReset() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13.1);
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(25.2, offset(1e-6));
    Aggregator mergedAggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(0, offset(1e-6));
    assertThat(getPoint(mergedAggregator).getValue()).isCloseTo(25.2, offset(1e-6));
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(-25.2);
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isCloseTo(0, offset(1e-6));
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
}
