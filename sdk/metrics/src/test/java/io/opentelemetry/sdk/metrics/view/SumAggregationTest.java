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

import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
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
          .isEqualTo(Descriptor.Type.MONOTONIC_DOUBLE);
      assertThat(sum.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(Descriptor.Type.MONOTONIC_LONG);
    }
    for (InstrumentType type : NON_MONOTONIC_INSTRUMENTS) {
      assertThat(sum.getDescriptorType(type, InstrumentValueType.DOUBLE))
          .isEqualTo(Descriptor.Type.NON_MONOTONIC_DOUBLE);
      assertThat(sum.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(Descriptor.Type.NON_MONOTONIC_LONG);
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
