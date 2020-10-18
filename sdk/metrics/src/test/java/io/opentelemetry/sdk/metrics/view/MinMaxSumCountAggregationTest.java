/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

class MinMaxSumCountAggregationTest {

  @Test
  void getDescriptorType() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE))
        .isEqualTo(MetricData.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG))
        .isEqualTo(MetricData.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_OBSERVER, InstrumentValueType.DOUBLE))
        .isEqualTo(MetricData.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(MetricData.Type.SUMMARY);
  }

  @Test
  void getAggregatorFactory() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    assertThat(minMaxSumCount.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(LongMinMaxSumCount.getFactory().getClass());
    assertThat(minMaxSumCount.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleMinMaxSumCount.getFactory().getClass());
  }

  @Test
  void availableForInstrument() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    for (InstrumentType type : InstrumentType.values()) {
      if (type == InstrumentType.VALUE_OBSERVER || type == InstrumentType.VALUE_RECORDER) {
        assertThat(minMaxSumCount.availableForInstrument(type)).isTrue();
      } else {
        assertThat(minMaxSumCount.availableForInstrument(type)).isFalse();
      }
    }
  }
}
