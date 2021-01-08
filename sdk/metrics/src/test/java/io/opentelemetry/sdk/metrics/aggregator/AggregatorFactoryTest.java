/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import org.junit.jupiter.api.Test;

class AggregatorFactoryTest {
  @Test
  void getCountAggregatorFactory() {
    AggregatorFactory count = AggregatorFactory.count();
    assertThat(count.create(InstrumentValueType.LONG))
        .isInstanceOf(CountAggregator.getInstance().getClass());
    assertThat(count.create(InstrumentValueType.DOUBLE))
        .isInstanceOf(CountAggregator.getInstance().getClass());
  }

  @Test
  void getLastValueAggregatorFactory() {
    AggregatorFactory lastValue = AggregatorFactory.lastValue();
    assertThat(lastValue.create(InstrumentValueType.LONG))
        .isInstanceOf(LongLastValueAggregator.getInstance().getClass());
    assertThat(lastValue.create(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleLastValueAggregator.getInstance().getClass());
  }

  @Test
  void getMinMaxSumCountAggregatorFactory() {
    AggregatorFactory minMaxSumCount = AggregatorFactory.minMaxSumCount();
    assertThat(minMaxSumCount.create(InstrumentValueType.LONG))
        .isInstanceOf(LongMinMaxSumCountAggregator.getInstance().getClass());
    assertThat(minMaxSumCount.create(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleMinMaxSumCountAggregator.getInstance().getClass());
  }

  @Test
  void getSumAggregatorFactory() {
    AggregatorFactory sum = AggregatorFactory.sum();
    assertThat(sum.create(InstrumentValueType.LONG))
        .isInstanceOf(LongSumAggregator.getInstance().getClass());
    assertThat(sum.create(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleSumAggregator.getInstance().getClass());
  }
}
