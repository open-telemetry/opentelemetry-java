/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkMeterProvider}. */
class SdkMeterRegistryTest {
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider meterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(Resource.getEmpty()).build();

  @Test
  void builder_HappyPath() {
    assertThat(
            SdkMeterProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .build())
        .isNotNull();
  }

  @Test
  void builder_NullClock() {
    assertThatThrownBy(() -> SdkMeterProvider.builder().setClock(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("clock");
  }

  @Test
  void builder_NullResource() {
    assertThatThrownBy(() -> SdkMeterProvider.builder().setResource(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("resource");
  }

  @Test
  void defaultGet() {
    assertThat(meterProvider.get("test")).isInstanceOf(SdkMeter.class);
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(meterProvider.get("test")).isSameAs(meterProvider.get("test"));
    assertThat(meterProvider.get("test")).isSameAs(meterProvider.get("test", null));
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(meterProvider.get("test", "version")).isSameAs(meterProvider.get("test", "version"));
  }

  @Test
  void propagatesInstrumentationLibraryInfoToMeter() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    SdkMeter meter = meterProvider.get(expected.getName(), expected.getVersion());
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  void metricProducer_GetAllMetrics() {
    SdkMeter sdkMeter1 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounterSdk longCounter1 = sdkMeter1.longCounterBuilder("testLongCounter").build();
    longCounter1.add(10, Labels.empty());
    SdkMeter sdkMeter2 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounterSdk longCounter2 = sdkMeter2.longCounterBuilder("testLongCounter").build();
    longCounter2.add(10, Labels.empty());

    assertThat(meterProvider.collectAllMetrics())
        .containsExactlyInAnyOrder(
            MetricData.createLongSum(
                Resource.getEmpty(),
                sdkMeter1.getInstrumentationLibraryInfo(),
                "testLongCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10)))),
            MetricData.createLongSum(
                Resource.getEmpty(),
                sdkMeter2.getInstrumentationLibraryInfo(),
                "testLongCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10)))));
  }

  @Test
  void suppliesDefaultMeterForNullName() {
    SdkMeter meter = meterProvider.get(null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = meterProvider.get(null, null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }

  @Test
  void suppliesDefaultMeterForEmptyName() {
    SdkMeter meter = meterProvider.get("");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = meterProvider.get("", "");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }
}
