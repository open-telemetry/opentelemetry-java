/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongSumAggregator}. */
class LongSumAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = LongSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(LongSumAggregator.class);
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(12 * 5));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }

  @Test
  void multipleRecords_WithNegatives() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(-23);
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    aggregator.recordLong(-11);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(14));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }

  @Test
  void toAccumulationAndReset() {
    Aggregator aggregator = LongSumAggregator.getFactory().getAggregator();
    aggregator.recordLong(13);
    aggregator.recordLong(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(25));
    assertThat(aggregator.accumulateThenReset()).isNull();
    aggregator.recordLong(12);
    aggregator.recordLong(-25);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(-13));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }
}
