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

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractInstrument}. */
@RunWith(JUnit4.class)
public class AbstractInstrumentTest {
  private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "description",
          "1",
          Collections.singletonMap("key_2", "value_2"),
          Collections.singletonList("key"));
  private static final MeterProviderSharedState METER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("test_abstract_instrument", "");
  private static final ActiveBatcher ACTIVE_BATCHER = new ActiveBatcher(Batchers.getNoop());

  @Test
  public void getValues() {
    TestInstrument testInstrument =
        new TestInstrument(
            INSTRUMENT_DESCRIPTOR,
            METER_SHARED_STATE,
            INSTRUMENTATION_LIBRARY_INFO,
            ACTIVE_BATCHER);
    assertThat(testInstrument.getDescriptor()).isSameInstanceAs(INSTRUMENT_DESCRIPTOR);
    assertThat(testInstrument.getMeterProviderSharedState()).isSameInstanceAs(METER_SHARED_STATE);
    assertThat(testInstrument.getInstrumentationLibraryInfo())
        .isSameInstanceAs(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(testInstrument.getActiveBatcher()).isSameInstanceAs(ACTIVE_BATCHER);
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterSharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        ActiveBatcher activeBatcher) {
      super(descriptor, meterSharedState, instrumentationLibraryInfo, activeBatcher);
    }

    @Override
    List<MetricData> collect() {
      return Collections.emptyList();
    }
  }
}
