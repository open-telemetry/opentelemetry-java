/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MeterSdkProvider}. */
class MeterSdkRegistryTest {
  private final TestClock testClock = TestClock.create();
  private final MeterSdkProvider meterProvider =
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
    assertThat(meterProvider.get("test")).isInstanceOf(MeterSdk.class);
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
    MeterSdk meter = meterProvider.get(expected.getName(), expected.getVersion());
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  void metricProducer_GetAllMetrics() {
    MeterSdk meterSdk1 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounterSdk longCounter1 = meterSdk1.longCounterBuilder("testLongCounter").build();
    longCounter1.add(10, Labels.empty());
    MeterSdk meterSdk2 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounterSdk longCounter2 = meterSdk2.longCounterBuilder("testLongCounter").build();
    longCounter2.add(10, Labels.empty());

    assertThat(meterProvider.getMetricProducer().collectAllMetrics())
        .containsExactlyInAnyOrder(
            MetricData.createLongSum(
                Resource.getEmpty(),
                meterSdk1.getInstrumentationLibraryInfo(),
                "testLongCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10)))),
            MetricData.createLongSum(
                Resource.getEmpty(),
                meterSdk2.getInstrumentationLibraryInfo(),
                "testLongCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10)))));
  }

  @Test
  void suppliesDefaultMeterForNullName() {
    MeterSdk meter = meterProvider.get(null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(MeterSdkProvider.DEFAULT_METER_NAME);

    meter = meterProvider.get(null, null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(MeterSdkProvider.DEFAULT_METER_NAME);
  }

  @Test
  void suppliesDefaultMeterForEmptyName() {
    MeterSdk meter = meterProvider.get("");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(MeterSdkProvider.DEFAULT_METER_NAME);

    meter = meterProvider.get("", "");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(MeterSdkProvider.DEFAULT_METER_NAME);
  }
}
