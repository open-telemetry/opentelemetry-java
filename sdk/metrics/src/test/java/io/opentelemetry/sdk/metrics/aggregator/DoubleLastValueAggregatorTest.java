/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AggregatorHandle}. */
class DoubleLastValueAggregatorTest {
  @Test
  void createHandle() {
    assertThat(Aggregator.doubleLastValue().createHandle())
        .isInstanceOf(DoubleLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<DoubleAccumulation> aggregatorHandle =
        Aggregator.doubleLastValue().createHandle();
    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(12.1));
    aggregatorHandle.recordDouble(13.1);
    aggregatorHandle.recordDouble(14.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(14.1));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<DoubleAccumulation> aggregatorHandle =
        Aggregator.doubleLastValue().createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(13.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(13.1));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(12.1));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }
}
