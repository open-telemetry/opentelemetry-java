/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Aggregations#count()}. */
class CountAggregationTest {
  @Test
  void getDescriptorType() {
    Aggregation count = Aggregations.count();
    for (InstrumentType type : InstrumentType.values()) {
      assertThat(count.getDescriptorType(type, InstrumentValueType.DOUBLE))
          .isEqualTo(MetricData.Type.SUM_LONG);
      assertThat(count.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(MetricData.Type.SUM_LONG);
    }
  }

  @Test
  void getAggregatorFactory() {
    // TODO: Change this to CountAggregator when available.
    Aggregation count = Aggregations.count();
    assertThat(count.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(NoopAggregator.getFactory().getClass());
    assertThat(count.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(NoopAggregator.getFactory().getClass());
  }

  @Test
  void availableForInstrument() {
    Aggregation count = Aggregations.count();
    for (InstrumentType type : InstrumentType.values()) {
      assertThat(count.availableForInstrument(type)).isTrue();
    }
  }
}
