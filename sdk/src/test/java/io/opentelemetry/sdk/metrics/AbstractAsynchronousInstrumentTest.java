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

import com.google.common.testing.EqualsTester;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class AbstractAsynchronousInstrumentTest {
  @Test
  public void attributeValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        new TestObserverInstrument(InstrumentValueType.LONG, InstrumentType.SUM_OBSERVER),
        new TestObserverInstrument(InstrumentValueType.LONG, InstrumentType.SUM_OBSERVER));
    tester.addEqualityGroup(
        new TestObserverInstrument(InstrumentValueType.LONG, InstrumentType.UP_DOWN_SUM_OBSERVER),
        new TestObserverInstrument(InstrumentValueType.LONG, InstrumentType.UP_DOWN_SUM_OBSERVER));
    tester.addEqualityGroup(
        new TestObserverInstrument(InstrumentValueType.DOUBLE, InstrumentType.SUM_OBSERVER),
        new TestObserverInstrument(InstrumentValueType.DOUBLE, InstrumentType.SUM_OBSERVER));
    tester.addEqualityGroup(
        new TestObserverInstrument(InstrumentValueType.DOUBLE, InstrumentType.UP_DOWN_SUM_OBSERVER),
        new TestObserverInstrument(
            InstrumentValueType.DOUBLE, InstrumentType.UP_DOWN_SUM_OBSERVER));
    tester.testEquals();
  }

  private static final class TestObserverInstrument extends AbstractAsynchronousInstrument {
    private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    private static final MeterSharedState METER_SHARED_STATE =
        MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());

    TestObserverInstrument(InstrumentValueType instrumentValueType, InstrumentType instrumentType) {
      super(
          InstrumentDescriptor.create(
              "name",
              "description",
              "1",
              Collections.singletonMap("key_2", "value_2"),
              instrumentType,
              instrumentValueType),
          METER_PROVIDER_SHARED_STATE,
          METER_SHARED_STATE);
    }

    @Override
    List<MetricData> collectAll() {
      return Collections.emptyList();
    }
  }
}
