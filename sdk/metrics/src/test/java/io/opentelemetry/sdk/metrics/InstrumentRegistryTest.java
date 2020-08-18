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

import static io.opentelemetry.sdk.common.InstrumentationLibraryInfo.getEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InstrumentRegistry}. */
class InstrumentRegistryTest {
  private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "description",
          "1",
          Labels.of("key_2", "value_2"),
          InstrumentType.COUNTER,
          InstrumentValueType.LONG);
  private static final InstrumentDescriptor OTHER_INSTRUMENT_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "other_description",
          "1",
          Labels.of("key_2", "value_2"),
          InstrumentType.COUNTER,
          InstrumentValueType.LONG);
  private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final ActiveBatcher ACTIVE_BATCHER = new ActiveBatcher(Batchers.getNoop());

  @Test
  void register() {
    MeterSharedState meterSharedState =
        MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());
    TestInstrument testInstrument =
        new TestInstrument(
            INSTRUMENT_DESCRIPTOR, METER_PROVIDER_SHARED_STATE, meterSharedState, ACTIVE_BATCHER);
    assertThat(meterSharedState.getInstrumentRegistry().register(testInstrument))
        .isSameAs(testInstrument);
    assertThat(meterSharedState.getInstrumentRegistry().register(testInstrument))
        .isSameAs(testInstrument);
    assertThat(
            meterSharedState
                .getInstrumentRegistry()
                .register(
                    new TestInstrument(
                        INSTRUMENT_DESCRIPTOR,
                        METER_PROVIDER_SHARED_STATE,
                        meterSharedState,
                        ACTIVE_BATCHER)))
        .isSameAs(testInstrument);
  }

  @Test
  void register_OtherDescriptor() {
    MeterSharedState meterSharedState = MeterSharedState.create(getEmpty());
    TestInstrument testInstrument =
        new TestInstrument(
            INSTRUMENT_DESCRIPTOR, METER_PROVIDER_SHARED_STATE, meterSharedState, ACTIVE_BATCHER);
    assertThat(meterSharedState.getInstrumentRegistry().register(testInstrument))
        .isSameAs(testInstrument);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            meterSharedState
                .getInstrumentRegistry()
                .register(
                    new TestInstrument(
                        OTHER_INSTRUMENT_DESCRIPTOR,
                        METER_PROVIDER_SHARED_STATE,
                        meterSharedState,
                        ACTIVE_BATCHER)),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void register_OtherInstance() {
    MeterSharedState meterSharedState = MeterSharedState.create(getEmpty());
    TestInstrument testInstrument =
        new TestInstrument(
            INSTRUMENT_DESCRIPTOR, METER_PROVIDER_SHARED_STATE, meterSharedState, ACTIVE_BATCHER);
    assertThat(meterSharedState.getInstrumentRegistry().register(testInstrument))
        .isSameAs(testInstrument);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            meterSharedState
                .getInstrumentRegistry()
                .register(
                    new OtherTestInstrument(
                        INSTRUMENT_DESCRIPTOR,
                        METER_PROVIDER_SHARED_STATE,
                        meterSharedState,
                        ACTIVE_BATCHER)),
        "Instrument with same name and different descriptor already created.");
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        ActiveBatcher activeBatcher) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    }

    @Override
    List<MetricData> collectAll() {
      return Collections.emptyList();
    }
  }

  private static final class OtherTestInstrument extends AbstractInstrument {
    OtherTestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        ActiveBatcher activeBatcher) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    }

    @Override
    List<MetricData> collectAll() {
      return Collections.emptyList();
    }
  }
}
