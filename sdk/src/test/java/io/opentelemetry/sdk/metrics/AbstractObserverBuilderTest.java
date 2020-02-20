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

import io.opentelemetry.metrics.Observer;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractObserver.Builder}. */
@RunWith(JUnit4.class)
public class AbstractObserverBuilderTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final MeterProviderSharedState METER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.getEmpty();

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    assertThat(testInstrumentBuilder.isMonotonic()).isFalse();
    assertThat(testInstrumentBuilder.getMeterProviderSharedState()).isEqualTo(METER_SHARED_STATE);
    assertThat(testInstrumentBuilder.getInstrumentationLibraryInfo())
        .isEqualTo(INSTRUMENTATION_LIBRARY_INFO);

    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEmpty();
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo("1");
    assertThat(testInstrument.getDescriptor().getLabelKeys()).isEmpty();
    assertThat(testInstrument.getDescriptor().getConstantLabels()).isEmpty();
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
            .setMonotonic(true);
    assertThat(testInstrumentBuilder.isMonotonic()).isTrue();
    assertThat(testInstrumentBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  private static final class TestInstrumentBuilder
      extends AbstractObserver.Builder<TestInstrumentBuilder, TestInstrument> {
    TestInstrumentBuilder(
        String name,
        MeterProviderSharedState sharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      super(name, sharedState, instrumentationLibraryInfo);
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
          getInstrumentationLibraryInfo(),
          isMonotonic());
    }
  }

  private static final class TestInstrument extends AbstractObserver
      implements Observer<TestResult> {

    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterSharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        boolean monotonic) {
      super(
          descriptor,
          InstrumentValueType.LONG,
          meterSharedState,
          instrumentationLibraryInfo,
          monotonic);
    }

    @Override
    public void setCallback(Callback<TestResult> metricUpdater) {}
  }

  private static final class TestResult {}
}
