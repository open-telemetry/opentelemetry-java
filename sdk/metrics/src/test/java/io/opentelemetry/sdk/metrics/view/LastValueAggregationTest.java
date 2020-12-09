/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Aggregations#sum()}. */
class LastValueAggregationTest {
  private static final EnumSet<InstrumentType> SUPPORTED_INSTRUMENTS =
      EnumSet.of(InstrumentType.SUM_OBSERVER, InstrumentType.UP_DOWN_SUM_OBSERVER);

  @Test
  void getDescriptorType_ForSupportedInstruments() {
    Aggregation lastValue = Aggregations.lastValue();
    assertThat(lastValue.getDescriptorType(InstrumentType.SUM_OBSERVER, InstrumentValueType.DOUBLE))
        .isEqualTo(MetricData.Type.SUM_DOUBLE);
    assertThat(lastValue.getDescriptorType(InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(MetricData.Type.SUM_LONG);
    assertThat(
            lastValue.getDescriptorType(
                InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE))
        .isEqualTo(MetricData.Type.NON_MONOTONIC_SUM_DOUBLE);
    assertThat(
            lastValue.getDescriptorType(
                InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(MetricData.Type.NON_MONOTONIC_SUM_LONG);
  }

  @Test
  void getAggregatorFactory() {
    Aggregation lastValue = Aggregations.lastValue();
    assertThat(lastValue.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(LongLastValueAggregator.getFactory().getClass());
    assertThat(lastValue.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleLastValueAggregator.getFactory().getClass());
  }

  @Test
  void availableForInstrument() {
    Aggregation lastValue = Aggregations.lastValue();
    for (InstrumentType type : InstrumentType.values()) {
      if (SUPPORTED_INSTRUMENTS.contains(type)) {
        assertThat(lastValue.availableForInstrument(type)).isTrue();
      } else {
        assertThat(lastValue.availableForInstrument(type)).isFalse();
      }
    }
  }
}
