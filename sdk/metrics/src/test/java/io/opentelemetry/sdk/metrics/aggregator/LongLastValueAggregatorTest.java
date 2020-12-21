/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
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
    assertThat(aggregator.accumulateThenReset()).isNull();
  }

  @Test
  void multipleRecords() {
    Aggregator aggregator = LongLastValueAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(12));
    aggregator.recordLong(13);
    aggregator.recordLong(14);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(14));
  }

  @Test
  void toAccumulationAndReset() {
    Aggregator aggregator = LongLastValueAggregator.getFactory().getAggregator();
    aggregator.recordLong(13);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(13));
    assertThat(aggregator.accumulateThenReset()).isNull();
    aggregator.recordLong(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(12));
    assertThat(aggregator.accumulateThenReset()).isNull();
  }
}
