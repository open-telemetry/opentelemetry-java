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
  void createHandle() {
    assertThat(Aggregator.longLastValue().createHandle())
        .isInstanceOf(LongLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = Aggregator.longLastValue().createHandle();
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(12));
    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(14);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(14));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = Aggregator.longLastValue().createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(13);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(13));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(LongAccumulation.create(12));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }
}
