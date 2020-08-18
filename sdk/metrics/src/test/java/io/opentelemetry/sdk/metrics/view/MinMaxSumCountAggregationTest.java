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

import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import org.junit.jupiter.api.Test;

class MinMaxSumCountAggregationTest {

  @Test
  void getDescriptorType() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE))
        .isEqualTo(Descriptor.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG))
        .isEqualTo(Descriptor.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_OBSERVER, InstrumentValueType.DOUBLE))
        .isEqualTo(Descriptor.Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.VALUE_OBSERVER, InstrumentValueType.LONG))
        .isEqualTo(Descriptor.Type.SUMMARY);
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
