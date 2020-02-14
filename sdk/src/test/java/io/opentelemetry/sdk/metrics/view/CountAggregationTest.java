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

import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Aggregations#count()}. */
@RunWith(JUnit4.class)
public class CountAggregationTest {
  @Test
  public void getDescriptorType() {
    Aggregation count = Aggregations.count();
    for (InstrumentType type : InstrumentType.values()) {
      assertThat(count.getDescriptorType(type, InstrumentValueType.DOUBLE))
          .isEqualTo(Type.MONOTONIC_LONG);
      assertThat(count.getDescriptorType(type, InstrumentValueType.LONG))
          .isEqualTo(Type.MONOTONIC_LONG);
    }
  }

  @Test
  public void getAggregatorFactory() {
    // TODO: Change this to CountAggregator when available.
    Aggregation count = Aggregations.count();
    assertThat(count.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(NoopAggregator.getFactory().getClass());
    assertThat(count.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(NoopAggregator.getFactory().getClass());
  }

  @Test
  public void availableForInstrument() {
    Aggregation count = Aggregations.count();
    for (InstrumentType type : InstrumentType.values()) {
      assertThat(count.availableForInstrument(type)).isTrue();
    }
  }
}
