/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongLastValueAggregator}. */
class LongLastValueAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = LongLastValueAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(LongLastValueAggregator.class);
  }

  @Test
  void toPoint() {
    Aggregator aggregator = LongLastValueAggregator.getFactory().getAggregator();
    assertNullPoint(aggregator);
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = LongLastValueAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(12);
    aggregator.recordLong(13);
    aggregator.recordLong(14);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(14);
  }

  @Test
  void mergeAndReset() {
    Aggregator aggregator = LongLastValueAggregator.getFactory().getAggregator();
    aggregator.recordLong(13);
    assertThat(getPoint(aggregator).getValue()).isEqualTo(13);
    Aggregator mergedAggregator = LongLastValueAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isEqualTo(13);
    aggregator.recordLong(12);
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isEqualTo(12);
  }

  private static LongPoint getPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get("key")).isEqualTo("value");
    assertThat(point).isInstanceOf(LongPoint.class);
    return (LongPoint) point;
  }

  private static void assertNullPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNull();
  }
}
