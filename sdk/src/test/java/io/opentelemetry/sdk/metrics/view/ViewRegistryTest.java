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
import static org.mockito.Mockito.when;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.Batcher;
import io.opentelemetry.sdk.metrics.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.MeterSharedState;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.ViewSpecification.Temporality;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ViewRegistryTest {

  @Mock private MeterSharedState meterSharedState;
  @Mock private MeterProviderSharedState meterProviderSharedState;

  @Before
  public void setUp() {
    when(meterProviderSharedState.getClock()).thenReturn(TestClock.create());
  }

  @Test
  public void defaultAggregations() {
    ViewRegistry viewRegistry = new ViewRegistry();

    verifyCorrect(
        viewRegistry,
        InstrumentType.VALUE_RECORDER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ true,
        DoubleMinMaxSumCount.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.VALUE_RECORDER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ true,
        LongMinMaxSumCount.class);

    verifyCorrect(
        viewRegistry,
        InstrumentType.VALUE_OBSERVER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ true,
        DoubleMinMaxSumCount.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.VALUE_OBSERVER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ true,
        LongMinMaxSumCount.class);

    verifyCorrect(
        viewRegistry,
        InstrumentType.COUNTER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleSumAggregator.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.COUNTER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ false,
        LongSumAggregator.class);

    verifyCorrect(
        viewRegistry,
        InstrumentType.UP_DOWN_COUNTER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleSumAggregator.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.UP_DOWN_COUNTER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ false,
        LongSumAggregator.class);

    verifyCorrect(
        viewRegistry,
        InstrumentType.SUM_OBSERVER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleLastValueAggregator.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.SUM_OBSERVER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ false,
        LongLastValueAggregator.class);

    verifyCorrect(
        viewRegistry,
        InstrumentType.UP_DOWN_SUM_OBSERVER,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleLastValueAggregator.class);
    verifyCorrect(
        viewRegistry,
        InstrumentType.UP_DOWN_SUM_OBSERVER,
        InstrumentValueType.LONG,
        /* expectedDeltas=*/ false,
        LongLastValueAggregator.class);
  }

  @Test
  public void selectByInstrumentType() {
    ViewRegistry viewRegistry = new ViewRegistry();

    InstrumentType instrumentType = InstrumentType.VALUE_RECORDER;

    InstrumentSelector selector = InstrumentSelector.create(instrumentType);
    ViewSpecification view = ViewSpecification.create(Aggregations.sum(), Temporality.CUMULATIVE);
    viewRegistry.registerView(selector, view);

    verifyCorrect(
        viewRegistry,
        instrumentType,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleSumAggregator.class);
  }

  private void verifyCorrect(
      ViewRegistry viewRegistry,
      InstrumentType instrumentType,
      InstrumentValueType valueType,
      boolean expectedDeltas,
      Class<?> expectedAggregator) {
    Batcher batcher =
        viewRegistry.createBatcher(
            meterProviderSharedState,
            meterSharedState,
            createDescriptor(instrumentType, valueType));

    assertThat(batcher.generatesDeltas()).isEqualTo(expectedDeltas);
    assertThat(batcher.getAggregator()).isInstanceOf(expectedAggregator);
  }

  private static InstrumentDescriptor createDescriptor(
      InstrumentType instrumentType, InstrumentValueType instrumentValueType) {
    return InstrumentDescriptor.create(
        "foo", "foo desc", "ms", Labels.empty(), instrumentType, instrumentValueType);
  }
}
