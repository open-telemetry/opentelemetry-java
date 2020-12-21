/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CountAggregator}. */
class CountAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = CountAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(CountAggregator.class);
  }

  @Test
  void recordLongOperations() {
    Aggregator aggregator = CountAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordLong(12);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(2));
  }

  @Test
  void recordDoubleOperations() {
    Aggregator aggregator = CountAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.3);
    aggregator.recordDouble(12.3);
    assertThat(aggregator.accumulateThenReset()).isEqualTo(LongAccumulation.create(2));
  }
}
