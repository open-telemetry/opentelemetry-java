/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkMeterProvider}. */
class SdkMeterRegistryTest {
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider meterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(Resource.empty())
          .registerMetricReader(sdkMeterReader)
          .build();

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
    assertThat(meterProvider.get("test")).isSameAs(meterProvider.meterBuilder("test").build());
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(meterProvider.meterBuilder("test").setInstrumentationVersion("version").build())
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
  void propagatesInstrumentationScopeInfoToMeter() {
    InstrumentationScopeInfo expected =
        InstrumentationScopeInfo.builder("theName")
            .setVersion("theVersion")
            .setSchemaUrl("http://theschema")
            .build();
    SdkMeter meter =
        (SdkMeter)
            meterProvider
                .meterBuilder(expected.getName())
                .setInstrumentationVersion(expected.getVersion())
                .setSchemaUrl(expected.getSchemaUrl())
                .build();
    assertThat(meter.getInstrumentationScopeInfo()).isEqualTo(expected);
  }

  @Test
  void metricProducer_GetAllMetrics() {
    Meter sdkMeter1 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_1");
    LongCounter longCounter1 = sdkMeter1.counterBuilder("testLongCounter").build();
    longCounter1.add(10, Attributes.empty());
    Meter sdkMeter2 = meterProvider.get("io.opentelemetry.sdk.metrics.MeterSdkRegistryTest_2");
    LongCounter longCounter2 = sdkMeter2.counterBuilder("testLongCounter").build();
    longCounter2.add(10, Attributes.empty());

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testLongCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(10)
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now()))))
        .extracting(MetricData::getInstrumentationScopeInfo)
        .containsExactlyInAnyOrder(
            ((SdkMeter) sdkMeter1).getInstrumentationScopeInfo(),
            ((SdkMeter) sdkMeter2).getInstrumentationScopeInfo());
  }

  @Test
  void suppliesDefaultMeterForNullName() {
    SdkMeter meter = (SdkMeter) meterProvider.get(null);
    assertThat(meter.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.meterBuilder(null).build();
    assertThat(meter.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }

  @Test
  void suppliesDefaultMeterForEmptyName() {
    SdkMeter meter = (SdkMeter) meterProvider.get("");
    assertThat(meter.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);

    meter = (SdkMeter) meterProvider.meterBuilder("").build();
    assertThat(meter.getInstrumentationScopeInfo().getName())
        .isEqualTo(SdkMeterProvider.DEFAULT_METER_NAME);
  }
}
