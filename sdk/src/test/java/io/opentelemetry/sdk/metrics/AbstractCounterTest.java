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

import com.google.common.testing.EqualsTester;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Test;

public class AbstractCounterTest {
  private static final boolean MONOTONIC = true;
  private static final boolean NON_MONOTONIC = false;

  @Test
  public void getValues() {
    assertThat(new TestCounterInstrument(InstrumentValueType.LONG, MONOTONIC).isMonotonic())
        .isTrue();
    assertThat(new TestCounterInstrument(InstrumentValueType.LONG, NON_MONOTONIC).isMonotonic())
        .isFalse();
  }

  @Test
  public void attributeValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        new TestCounterInstrument(InstrumentValueType.LONG, MONOTONIC),
        new TestCounterInstrument(InstrumentValueType.LONG, MONOTONIC));
    tester.addEqualityGroup(
        new TestCounterInstrument(InstrumentValueType.LONG, NON_MONOTONIC),
        new TestCounterInstrument(InstrumentValueType.LONG, NON_MONOTONIC));
    tester.addEqualityGroup(
        new TestCounterInstrument(InstrumentValueType.DOUBLE, MONOTONIC),
        new TestCounterInstrument(InstrumentValueType.DOUBLE, MONOTONIC));
    tester.addEqualityGroup(
        new TestCounterInstrument(InstrumentValueType.DOUBLE, NON_MONOTONIC),
        new TestCounterInstrument(InstrumentValueType.DOUBLE, NON_MONOTONIC));
    tester.testEquals();
  }

  private static final class TestCounterInstrument extends AbstractCounter<TestBoundCounter> {
    private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
        InstrumentDescriptor.create(
            "name", "description", "1", Collections.singletonMap("key_2", "value_2"));
    private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    private static final MeterSharedState METER_SHARED_STATE =
        MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());

    TestCounterInstrument(InstrumentValueType instrumentValueType, boolean monotonic) {
      super(
          INSTRUMENT_DESCRIPTOR,
          instrumentValueType,
          METER_PROVIDER_SHARED_STATE,
          METER_SHARED_STATE,
          monotonic);
    }

    @Override
    TestBoundCounter newBinding(Batcher batcher) {
      return new TestBoundCounter();
    }
  }

  private static final class TestBoundCounter extends AbstractBoundInstrument {

    TestBoundCounter() {
      super(NoopAggregator.getFactory().getAggregator());
    }
  }
}
