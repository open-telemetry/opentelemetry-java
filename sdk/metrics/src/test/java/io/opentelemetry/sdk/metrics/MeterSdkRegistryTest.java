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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MeterSdkProvider}. */
class MeterSdkRegistryTest {
  private final TestClock testClock = TestClock.create();
  private final MeterSdkProvider meterRegistry =
      MeterSdkProvider.builder().setClock(testClock).setResource(Resource.getEmpty()).build();

  @Test
  void builder_HappyPath() {
    assertThat(
            MeterSdkProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .build())
        .isNotNull();
  }

  @Test
  void builder_NullClock() {
    assertThrows(
        NullPointerException.class, () -> MeterSdkProvider.builder().setClock(null), "clock");
  }

  @Test
  void builder_NullResource() {
    assertThrows(
        NullPointerException.class, () -> MeterSdkProvider.builder().setResource(null), "resource");
  }

  @Test
  void defaultGet() {
    assertThat(meterRegistry.get("test")).isInstanceOf(MeterSdk.class);
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(meterRegistry.get("test")).isSameAs(meterRegistry.get("test"));
    assertThat(meterRegistry.get("test")).isSameAs(meterRegistry.get("test", null));
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(meterRegistry.get("test", "version")).isSameAs(meterRegistry.get("test", "version"));
  }

  @Test
  void propagatesInstrumentationLibraryInfoToMeter() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    MeterSdk meter = meterRegistry.get(expected.getName(), expected.getVersion());
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  void metricProducer_GetAllMetrics() {
    MeterSdk meterSdk1 = meterRegistry.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounterSdk longCounter1 = meterSdk1.longCounterBuilder("testLongCounter").build();
    longCounter1.add(10, Labels.empty());
    MeterSdk meterSdk2 = meterRegistry.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounterSdk longCounter2 = meterSdk2.longCounterBuilder("testLongCounter").build();
    longCounter2.add(10, Labels.empty());

    assertThat(meterRegistry.getMetricProducer().collectAllMetrics())
        .containsExactlyInAnyOrder(
            MetricData.create(
                Descriptor.create(
                    "testLongCounter", "", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
                Resource.getEmpty(),
                meterSdk1.getInstrumentationLibraryInfo(),
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10))),
            MetricData.create(
                Descriptor.create(
                    "testLongCounter", "", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
                Resource.getEmpty(),
                meterSdk2.getInstrumentationLibraryInfo(),
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10))));
  }
}
