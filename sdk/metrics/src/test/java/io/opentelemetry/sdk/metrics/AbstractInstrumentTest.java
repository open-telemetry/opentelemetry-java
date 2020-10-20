/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AbstractInstrument}. */
class AbstractInstrumentTest {
  private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "1", InstrumentType.COUNTER, InstrumentValueType.LONG);
  private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("test_abstract_instrument", "");
  private static final MeterSharedState METER_SHARED_STATE =
      MeterSharedState.create(INSTRUMENTATION_LIBRARY_INFO);
  private static final ActiveBatcher ACTIVE_BATCHER = new ActiveBatcher(Batchers.getNoop());

  @Test
  void getValues() {
    TestInstrument testInstrument =
        new TestInstrument(
            INSTRUMENT_DESCRIPTOR, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE, ACTIVE_BATCHER);
    assertThat(testInstrument.getDescriptor()).isSameAs(INSTRUMENT_DESCRIPTOR);
    assertThat(testInstrument.getMeterProviderSharedState()).isSameAs(METER_PROVIDER_SHARED_STATE);
    assertThat(testInstrument.getMeterSharedState()).isSameAs(METER_SHARED_STATE);
    assertThat(testInstrument.getActiveBatcher()).isSameAs(ACTIVE_BATCHER);
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
}
