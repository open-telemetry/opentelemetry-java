/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Aggregations#sum()}. */
class SumAggregationTest {
  private static final InstrumentType[] MONOTONIC_INSTRUMENTS = {
    InstrumentType.COUNTER, InstrumentType.SUM_OBSERVER
  };
  private static final InstrumentType[] NON_MONOTONIC_INSTRUMENTS = {
    InstrumentType.UP_DOWN_COUNTER,
    InstrumentType.UP_DOWN_SUM_OBSERVER,
    InstrumentType.VALUE_RECORDER,
    InstrumentType.VALUE_OBSERVER
  };

  @Test
  void getDescriptorType() {
    Aggregation sum = Aggregations.sum();
    for (InstrumentType type : MONOTONIC_INSTRUMENTS) {
      assertThat(sum.getDescriptorType(type, InstrumentValueType.DOUBLE))
          .isEqualTo(MetricData.Type.SUM_DOUBLE);
      assertThat(sum.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(MetricData.Type.SUM_LONG);
    }
    for (InstrumentType type : NON_MONOTONIC_INSTRUMENTS) {
      assertThat(sum.getDescriptorType(type, InstrumentValueType.DOUBLE))
          .isEqualTo(MetricData.Type.NON_MONOTONIC_SUM_DOUBLE);
      assertThat(sum.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(MetricData.Type.NON_MONOTONIC_SUM_LONG);
    }
  }

  @Test
  void getAggregatorFactory() {
    Aggregation sum = Aggregations.sum();
    assertThat(sum.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(LongSumAggregator.getFactory().getClass());
    assertThat(sum.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleSumAggregator.getFactory().getClass());
  }

  @Test
  void availableForInstrument() {
    Aggregation sum = Aggregations.sum();
    for (InstrumentType type : InstrumentType.values()) {
      assertThat(sum.availableForInstrument(type)).isTrue();
    }
  }
}
