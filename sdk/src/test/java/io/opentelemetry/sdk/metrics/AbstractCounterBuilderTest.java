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
import io.opentelemetry.metrics.InstrumentWithBinding.BoundInstrument;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
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
  private static final MeterProviderSharedState METER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.EMPTY;

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testMetricBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.getDescription()).isEmpty();
    assertThat(testMetricBuilder.getUnit()).isEqualTo("1");
    assertThat(testMetricBuilder.getLabelKeys()).isEmpty();
    assertThat(testMetricBuilder.getConstantLabels()).isEmpty();
    assertThat(testMetricBuilder.isMonotonic()).isTrue();
    assertThat(testMetricBuilder.getMeterProviderSharedState()).isEqualTo(METER_SHARED_STATE);
    assertThat(testMetricBuilder.getInstrumentationLibraryInfo())
        .isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testMetricBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
            .setMonotonic(false);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.isMonotonic()).isFalse();
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  private static final class TestInstrumentBuilder
      extends AbstractCounter.Builder<TestInstrumentBuilder, TestInstrument> {
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
      return new TestInstrument();
    }
  }

  private static final class TestInstrument implements Counter<TestBoundInstrument> {
    private static final TestBoundInstrument HANDLE = new TestBoundInstrument();

    @Override
    public TestBoundInstrument bind(LabelSet labelSet) {
      return HANDLE;
    }
  }

  private static final class TestBoundInstrument implements BoundInstrument {
    @Override
    public void unbind() {}
  }
}
