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

public class AbstractMeasureTest {
  private static final boolean ABSOLUTE = true;
  private static final boolean NON_ABSOLUTE = false;

  @Test
  public void getValues() {
    assertThat(new TestMeasureInstrument(InstrumentValueType.LONG, ABSOLUTE).isAbsolute()).isTrue();
    assertThat(new TestMeasureInstrument(InstrumentValueType.LONG, NON_ABSOLUTE).isAbsolute())
        .isFalse();
  }

  @Test
  public void attributeValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        new TestMeasureInstrument(InstrumentValueType.LONG, ABSOLUTE),
        new TestMeasureInstrument(InstrumentValueType.LONG, ABSOLUTE));
    tester.addEqualityGroup(
        new TestMeasureInstrument(InstrumentValueType.LONG, NON_ABSOLUTE),
        new TestMeasureInstrument(InstrumentValueType.LONG, NON_ABSOLUTE));
    tester.addEqualityGroup(
        new TestMeasureInstrument(InstrumentValueType.DOUBLE, ABSOLUTE),
        new TestMeasureInstrument(InstrumentValueType.DOUBLE, ABSOLUTE));
    tester.addEqualityGroup(
        new TestMeasureInstrument(InstrumentValueType.DOUBLE, NON_ABSOLUTE),
        new TestMeasureInstrument(InstrumentValueType.DOUBLE, NON_ABSOLUTE));
    tester.testEquals();
  }

  private static final class TestMeasureInstrument extends AbstractMeasure<TestBoundMeasure> {
    private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
        InstrumentDescriptor.create(
            "name", "description", "1", Collections.singletonMap("key_2", "value_2"));
    private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    private static final MeterSharedState METER_SHARED_STATE =
        MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());

    TestMeasureInstrument(InstrumentValueType instrumentValueType, boolean absolute) {
      super(
          INSTRUMENT_DESCRIPTOR,
          instrumentValueType,
          METER_PROVIDER_SHARED_STATE,
          METER_SHARED_STATE,
          absolute);
    }

    @Override
    TestBoundMeasure newBinding(Batcher batcher) {
      return new TestBoundMeasure();
    }
  }

  private static final class TestBoundMeasure extends AbstractBoundInstrument {

    TestBoundMeasure() {
      super(NoopAggregator.getFactory().getAggregator());
    }
  }
}
