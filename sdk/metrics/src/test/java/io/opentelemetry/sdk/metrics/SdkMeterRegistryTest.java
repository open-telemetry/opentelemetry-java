/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkMeterProvider}. */
class SdkMeterRegistryTest {
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider meterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(Resource.empty()).build();

  @Test
  void builder_HappyPath() {
    assertThat(
            SdkMeterProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(Resource.empty())
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
    assertThat(meterProvider.get("test")).isSameAs(meterProvider.meterBuilder("test").build());
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(meterProvider.get("test", "version"))
        .isSameAs(meterProvider.get("test", "version"))
        .isSameAs(meterProvider.meterBuilder("test").setInstrumentationVersion("version").build());
  }

  @Test
  void getSameInstanceForSameName_WithVersionAndSchema() {
    assertThat(
            meterProvider
                .meterBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build())
        .isSameAs(
            meterProvider
                .meterBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void propagatesInstrumentationLibraryInfoToMeter() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion", "http://theschema");
    SdkMeter meter =
        (SdkMeter)
            meterProvider
                .meterBuilder(expected.getName())
                .setInstrumentationVersion(expected.getVersion())
                .setSchemaUrl(expected.getSchemaUrl())
                .build();
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  @SuppressWarnings("unchecked")
  void metricProducer_GetAllMetrics() {
    Meter sdkMeter1 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounter longCounter1 = sdkMeter1.longCounterBuilder("testLongCounter").build();
    longCounter1.add(10, Labels.empty());
    Meter sdkMeter2 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounter longCounter2 = sdkMeter2.longCounterBuilder("testLongCounter").build();
    longCounter2.add(10, Labels.empty());

    assertThat(meterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testLongCounter")
                    .hasLongSum()
                    .isCumulative()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasValue(10)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())))
        .extracting(metric -> metric.getInstrumentationLibraryInfo())
        .containsExactlyInAnyOrder(
            ((SdkMeter) sdkMeter1).getInstrumentationLibraryInfo(),
            ((SdkMeter) sdkMeter2).getInstrumentationLibraryInfo());
  }

  @Test
  void suppliesDefaultMeterForNullName() {
    SdkMeter meter = (SdkMeter) meterProvider.get(null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.get(null, null);
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.meterBuilder(null).build();
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }

  @Test
  void suppliesDefaultMeterForEmptyName() {
    SdkMeter meter = (SdkMeter) meterProvider.get("");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.get("", "");
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.meterBuilder("").build();
    assertThat(meter.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }
}
