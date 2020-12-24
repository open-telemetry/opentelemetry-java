/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumAggregator}. */
class DoubleSumAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory<DoubleAccumulation> factory = DoubleSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleSumAggregator.class);
  }

  @Test
  void toPoint() {
    Aggregator<DoubleAccumulation> aggregator = DoubleSumAggregator.getFactory().getAggregator();
    assertThat(aggregator.accumulateThenReset()).isNull();
  }

  @Test
  void multipleRecords() {
    Aggregator<DoubleAccumulation> aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(12.1 * 5));
  }

  @Test
  void multipleRecords_WithNegatives() {
    Aggregator<DoubleAccumulation> aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12);
    aggregator.recordDouble(12);
    aggregator.recordDouble(-23);
    aggregator.recordDouble(12);
    aggregator.recordDouble(12);
    aggregator.recordDouble(-11);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(14));
  }

  @Test
  void toAccumulationAndReset() {
    Aggregator<DoubleAccumulation> aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13);
    aggregator.recordDouble(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(25));
    assertThat(aggregator.accumulateThenReset()).isNull();
    aggregator.recordDouble(12);
    aggregator.recordDouble(-25);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(-13));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }
}
