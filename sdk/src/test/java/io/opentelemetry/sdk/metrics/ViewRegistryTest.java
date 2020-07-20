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

package io.opentelemetry.sdk.metrics;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration.Temporality;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
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

    InstrumentSelector selector =
        InstrumentSelector.newBuilder().instrumentType(instrumentType).build();
    AggregationConfiguration view =
        AggregationConfiguration.create(Aggregations.sum(), Temporality.CUMULATIVE);
    viewRegistry.registerView(selector, view);

    verifyCorrect(
        viewRegistry,
        instrumentType,
        InstrumentValueType.DOUBLE,
        /* expectedDeltas=*/ false,
        DoubleSumAggregator.class);
  }

  @Test
  public void selectByInstrumentName() {
    ViewRegistry viewRegistry = new ViewRegistry();

    InstrumentSelector selector =
        InstrumentSelector.newBuilder().instrumentNameRegex("http.*duration").build();
    AggregationConfiguration view =
        AggregationConfiguration.create(Aggregations.sum(), Temporality.CUMULATIVE);

    viewRegistry.registerView(selector, view);

    InstrumentType instrumentType = InstrumentType.VALUE_RECORDER;
    // this one matches on name
    verifyCorrect(
        viewRegistry,
        createDescriptor(instrumentType, InstrumentValueType.DOUBLE, "http.server.duration"),
        /* expectedDeltas= */ false,
        DoubleSumAggregator.class);
    // this one does not match on name
    verifyCorrect(
        viewRegistry,
        createDescriptor(instrumentType, InstrumentValueType.DOUBLE, "foo.bar.duration"),
        /* expectedDeltas=*/ true,
        DoubleMinMaxSumCount.class);
  }

  @Test
  public void selectByInstrumentNameAndType() {
    ViewRegistry viewRegistry = new ViewRegistry();

    InstrumentSelector selector =
        InstrumentSelector.newBuilder()
            .instrumentType(InstrumentType.VALUE_RECORDER)
            .instrumentNameRegex("http.*duration")
            .build();
    AggregationConfiguration view =
        AggregationConfiguration.create(Aggregations.sum(), Temporality.CUMULATIVE);

    viewRegistry.registerView(selector, view);

    // this one matches on name
    verifyCorrect(
        viewRegistry,
        createDescriptor(
            InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE, "http.server.duration"),
        /* expectedDeltas= */ false,
        DoubleSumAggregator.class);
    // this one does not match on name, but does on type, so should get the default
    verifyCorrect(
        viewRegistry,
        createDescriptor(
            InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE, "foo.bar.duration"),
        /* expectedDeltas=*/ true,
        DoubleMinMaxSumCount.class);
    // this one does not match on type, but does on name, so should get the default
    verifyCorrect(
        viewRegistry,
        createDescriptor(
            InstrumentType.SUM_OBSERVER, InstrumentValueType.DOUBLE, "http.bar.duration"),
        /* expectedDeltas=*/ false,
        DoubleLastValueAggregator.class);
  }

  private void verifyCorrect(
      ViewRegistry viewRegistry,
      InstrumentType instrumentType,
      InstrumentValueType valueType,
      boolean expectedDeltas,
      Class<?> expectedAggregator) {
    verifyCorrect(
        viewRegistry,
        createDescriptor(instrumentType, valueType, "foo"),
        expectedDeltas,
        expectedAggregator);
  }

  private void verifyCorrect(
      ViewRegistry viewRegistry,
      InstrumentDescriptor descriptor,
      boolean expectedDeltas,
      Class<?> expectedAggregator) {
    Batcher batcher =
        viewRegistry.createBatcher(meterProviderSharedState, meterSharedState, descriptor);

    assertThat(batcher.generatesDeltas()).isEqualTo(expectedDeltas);
    assertThat(batcher.getAggregator()).isInstanceOf(expectedAggregator);
  }

  private static InstrumentDescriptor createDescriptor(
      InstrumentType instrumentType,
      InstrumentValueType instrumentValueType,
      String instrumentName) {
    return InstrumentDescriptor.create(
        instrumentName, "foo desc", "ms", Labels.empty(), instrumentType, instrumentValueType);
  }
}
