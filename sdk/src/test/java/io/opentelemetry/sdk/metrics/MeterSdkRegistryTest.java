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
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeterSdkProvider}. */
@RunWith(JUnit4.class)
public class MeterSdkRegistryTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final TestClock testClock = TestClock.create();
  private final MeterSdkProvider meterRegistry =
      MeterSdkProvider.builder().setClock(testClock).setResource(Resource.getEmpty()).build();

  @Test
  public void builder_HappyPath() {
    assertThat(
            MeterSdkProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .build())
        .isNotNull();
  }

  @Test
  public void builder_NullClock() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("clock");
    MeterSdkProvider.builder().setClock(null);
  }

  @Test
  public void builder_NullResource() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("resource");
    MeterSdkProvider.builder().setResource(null);
  }

  @Test
  public void defaultGet() {
    assertThat(meterRegistry.get("test")).isInstanceOf(MeterSdk.class);
  }

  @Test
  public void getSameInstanceForSameName_WithoutVersion() {
    assertThat(meterRegistry.get("test")).isSameInstanceAs(meterRegistry.get("test"));
    assertThat(meterRegistry.get("test")).isSameInstanceAs(meterRegistry.get("test", null));
  }

  @Test
  public void getSameInstanceForSameName_WithVersion() {
    assertThat(meterRegistry.get("test", "version"))
        .isSameInstanceAs(meterRegistry.get("test", "version"));
  }

  @Test
  public void propagatesInstrumentationLibraryInfoToMeter() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    MeterSdk meter = meterRegistry.get(expected.getName(), expected.getVersion());
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  public void metricProducer_GetAllMetrics() {
    MeterSdk meterSdk1 = meterRegistry.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounterSdk longCounter1 = meterSdk1.longCounterBuilder("testLongCounter").build();
    longCounter1.add(10);
    MeterSdk meterSdk2 = meterRegistry.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounterSdk longCounter2 = meterSdk2.longCounterBuilder("testLongCounter").build();
    longCounter2.add(10);

    assertThat(meterRegistry.getMetricProducer().getAllMetrics())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testLongCounter",
                    "",
                    "1",
                    Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                Resource.getEmpty(),
                meterSdk1.getInstrumentationLibraryInfo(),
                Collections.<Point>singletonList(
                    LongPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        10))),
            MetricData.create(
                Descriptor.create(
                    "testLongCounter",
                    "",
                    "1",
                    Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                Resource.getEmpty(),
                meterSdk2.getInstrumentationLibraryInfo(),
                Collections.<Point>singletonList(
                    LongPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        10))));
  }
}
