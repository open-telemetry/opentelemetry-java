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
  void createHandle() {
    assertThat(Aggregator.count().createHandle()).isInstanceOf(CountAggregator.Handle.class);
  }

  @Test
  void toPoint() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = Aggregator.count().createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void recordLongOperations() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = Aggregator.count().createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(2));
  }

  @Test
  void recordDoubleOperations() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = Aggregator.count().createHandle();
    aggregatorHandle.recordDouble(12.3);
    aggregatorHandle.recordDouble(12.3);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(2));
  }
}
