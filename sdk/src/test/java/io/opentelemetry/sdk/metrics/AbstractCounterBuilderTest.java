/*
 * Copyright 2019, OpenTelemetry Authors
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

import io.opentelemetry.metrics.Counter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractCounter.Builder}. */
@RunWith(JUnit4.class)
public class AbstractCounterBuilderTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final MeterSharedState METER_SHARED_STATE =
      MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    assertThat(testInstrumentBuilder.isMonotonic()).isTrue();
    assertThat(testInstrumentBuilder.getMeterProviderSharedState())
        .isSameInstanceAs(METER_PROVIDER_SHARED_STATE);
    assertThat(testInstrumentBuilder.getMeterSharedState()).isSameInstanceAs(METER_SHARED_STATE);

    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEmpty();
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo("1");
    assertThat(testInstrument.getDescriptor().getConstantLabels()).isEmpty();
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
            .setMonotonic(false);
    assertThat(testInstrumentBuilder.isMonotonic()).isFalse();
    assertThat(testInstrumentBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  private static final class TestInstrumentBuilder
      extends AbstractCounter.Builder<TestInstrumentBuilder> {
    TestInstrumentBuilder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    TestInstrumentBuilder getThis() {
      return this;
    }

    @Override
    public TestInstrument build() {
      return new TestInstrument(
          getInstrumentDescriptor(),
          getMeterProviderSharedState(),
          getMeterSharedState(),
          isMonotonic());
    }
  }

  private static final class TestInstrument extends AbstractCounter<TestBoundCounter>
      implements Counter<TestBoundCounter> {
    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        boolean monotonic) {
      super(
          descriptor,
          InstrumentValueType.LONG,
          meterProviderSharedState,
          meterSharedState,
          monotonic);
    }

    @Override
    TestBoundCounter newBinding(Batcher batcher) {
      return new TestBoundCounter(NoopAggregator.getFactory().getAggregator());
    }

    @Override
    public TestBoundCounter bind(String... labelKeyValuePairs) {
      return bind(LabelSetSdk.create(labelKeyValuePairs));
    }
  }

  private static final class TestBoundCounter extends AbstractBoundInstrument {
    private TestBoundCounter(Aggregator aggregator) {
      super(aggregator);
    }
  }
}
