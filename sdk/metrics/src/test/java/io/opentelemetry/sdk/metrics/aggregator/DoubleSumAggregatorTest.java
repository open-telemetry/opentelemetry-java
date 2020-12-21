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
    AggregatorFactory factory = DoubleSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleSumAggregator.class);
  }

  @Test
  void toPoint() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    assertThat(aggregator.toAccumulationThenReset()).isNull();
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    assertThat(aggregator.toAccumulationThenReset()).isEqualTo(DoubleAccumulation.create(12.1 * 5));
  }

  @Test
  void multipleRecords_WithNegatives() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12);
    aggregator.recordDouble(12);
    aggregator.recordDouble(-23);
    aggregator.recordDouble(12);
    aggregator.recordDouble(12);
    aggregator.recordDouble(-11);
    assertThat(aggregator.toAccumulationThenReset()).isEqualTo(DoubleAccumulation.create(14));
  }

  @Test
  void toAccumulationAndReset() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13);
    aggregator.recordDouble(12);
    assertThat(aggregator.toAccumulationThenReset()).isEqualTo(DoubleAccumulation.create(25));
    assertThat(aggregator.toAccumulationThenReset()).isNull();
    aggregator.recordDouble(12);
    aggregator.recordDouble(-25);
    assertThat(aggregator.toAccumulationThenReset()).isEqualTo(DoubleAccumulation.create(-13));
    assertThat(aggregator.toAccumulationThenReset()).isNull();
  }
}
