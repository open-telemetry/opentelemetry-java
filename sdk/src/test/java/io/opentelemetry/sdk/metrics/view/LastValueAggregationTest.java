/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
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
        .isEqualTo(Descriptor.Type.MONOTONIC_DOUBLE);
    assertThat(lastValue.getDescriptorType(InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(Descriptor.Type.MONOTONIC_LONG);
    assertThat(
            lastValue.getDescriptorType(
                InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE))
        .isEqualTo(Descriptor.Type.NON_MONOTONIC_DOUBLE);
    assertThat(
            lastValue.getDescriptorType(
                InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(Descriptor.Type.NON_MONOTONIC_LONG);
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
