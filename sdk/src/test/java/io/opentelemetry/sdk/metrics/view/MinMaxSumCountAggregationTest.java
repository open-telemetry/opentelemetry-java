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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import org.junit.Test;

public class MinMaxSumCountAggregationTest {

  @Test
  public void getDescriptorType() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.MEASURE_NON_ABSOLUTE, InstrumentValueType.DOUBLE))
        .isEqualTo(Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.MEASURE_NON_ABSOLUTE, InstrumentValueType.LONG))
        .isEqualTo(Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.MEASURE_ABSOLUTE, InstrumentValueType.DOUBLE))
        .isEqualTo(Type.SUMMARY);
    assertThat(
            minMaxSumCount.getDescriptorType(
                InstrumentType.MEASURE_ABSOLUTE, InstrumentValueType.LONG))
        .isEqualTo(Type.SUMMARY);
  }

  @Test
  public void getAggregatorFactory() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    assertThat(minMaxSumCount.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(LongMinMaxSumCount.getFactory().getClass());
    assertThat(minMaxSumCount.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(DoubleMinMaxSumCount.getFactory().getClass());
  }

  @Test
  public void availableForInstrument() {
    Aggregation minMaxSumCount = Aggregations.minMaxSumCount();
    for (InstrumentType type : InstrumentType.values()) {
      if (type == InstrumentType.MEASURE_ABSOLUTE || type == InstrumentType.MEASURE_NON_ABSOLUTE) {
        assertThat(minMaxSumCount.availableForInstrument(type)).isTrue();
      } else {
        assertThat(minMaxSumCount.availableForInstrument(type)).isFalse();
      }
    }
  }
}
