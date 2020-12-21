/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
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
    assertThat(aggregator.accumulateThenReset()).isNull();
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(12.1));
    aggregator.recordDouble(13.1);
    aggregator.recordDouble(14.1);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(14.1));
  }

  @Test
  void toAccumulationAndReset() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13.1);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(13.1));
    assertThat(aggregator.accumulateThenReset()).isNull();
    aggregator.recordDouble(12.1);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(12.1));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }
}
