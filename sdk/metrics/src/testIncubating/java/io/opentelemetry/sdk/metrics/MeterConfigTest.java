/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameMatchesGlob;
import static io.opentelemetry.sdk.metrics.internal.MeterConfig.defaultConfig;
import static io.opentelemetry.sdk.metrics.internal.MeterConfig.disabled;
import static io.opentelemetry.sdk.metrics.internal.MeterConfig.enabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.stream.Collectors.groupingBy;

import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MeterConfigTest {

  @Test
  void disableScopes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            // Disable meterB. Since meters are enabled by default, meterA and meterC are enabled.
            .addMeterConfiguratorCondition(nameEquals("meterB"), disabled())
            .registerMetricReader(reader)
            // Register drop aggregation for all instruments of meterD. Instruments are disabled if
            // their relevant MeterConfig is disabled, or if there are no resolved views which
            // consume the measurements.
            .registerView(
                InstrumentSelector.builder().setMeterName("meterD").build(),
                View.builder().setAggregation(Aggregation.drop()).build())
            .build();

    Meter meterA = meterProvider.get("meterA");
    Meter meterB = meterProvider.get("meterB");
    Meter meterC = meterProvider.get("meterC");
    Meter meterD = meterProvider.get("meterD");
    AtomicLong meterAInvocations = new AtomicLong();
    AtomicLong meterBInvocations = new AtomicLong();
    AtomicLong meterCInvocations = new AtomicLong();
    AtomicLong meterDInvocations = new AtomicLong();

    // Record measurements to each instrument type
    recordToMeterInstruments(meterA, meterAInvocations);
    recordToMeterInstruments(meterB, meterBInvocations);
    recordToMeterInstruments(meterC, meterCInvocations);
    recordToMeterInstruments(meterD, meterDInvocations);

    // Only metrics from meterA and meterC should be seen
    assertThat(reader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream().collect(groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterA"))).hasSize(14);
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterB"))).isNull();
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterC"))).hasSize(14);
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterD"))).isNull();
            });
    // Only async callbacks from meterA and meterC should be invoked
    assertThat(meterAInvocations.get()).isPositive();
    assertThat(meterBInvocations.get()).isZero();
    assertThat(meterCInvocations.get()).isPositive();
    assertThat(meterDInvocations.get()).isZero();
    // Instruments from meterA and meterC are enabled, meterC is not enabled
    assertMeterInstrumentsEnabled(meterA, /* expectedEnabled= */ true);
    assertMeterInstrumentsEnabled(meterB, /* expectedEnabled= */ false);
    assertMeterInstrumentsEnabled(meterC, /* expectedEnabled= */ true);
    assertMeterInstrumentsEnabled(meterD, /* expectedEnabled= */ false);
  }

  private static void recordToMeterInstruments(Meter meter, AtomicLong asyncInvocationsCount) {
    meter.counterBuilder("longCounter").build().add(1);
    meter.counterBuilder("doubleCounter").ofDoubles().build().add(1);
    meter
        .counterBuilder("asyncLongCounter")
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1);
            });
    meter
        .counterBuilder("asyncDoubleCounter")
        .ofDoubles()
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1);
            });
    meter.upDownCounterBuilder("longUpDownCounter").build().add(1);
    meter.upDownCounterBuilder("doubleUpDownCounter").ofDoubles().build().add(1);
    meter
        .upDownCounterBuilder("asyncLongUpDownCounter")
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1);
            });
    meter
        .upDownCounterBuilder("asyncDoubleUpDownCounter")
        .ofDoubles()
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1);
            });
    meter.histogramBuilder("doubleHistogram").build().record(1.0);
    meter.histogramBuilder("longHistogram").ofLongs().build().record(1);
    meter.gaugeBuilder("doubleGauge").build().set(1);
    meter.gaugeBuilder("longGauge").ofLongs().build().set(1);
    meter
        .gaugeBuilder("asyncDoubleGauge")
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1.0);
            });
    meter
        .gaugeBuilder("asyncLongGauge")
        .ofLongs()
        .buildWithCallback(
            observable -> {
              asyncInvocationsCount.incrementAndGet();
              observable.record(1);
            });
  }

  private static void assertMeterInstrumentsEnabled(Meter meter, boolean expectedEnabled) {
    assertThat(
            ((ExtendedDoubleCounter) meter.counterBuilder("doubleCounter").ofDoubles().build())
                .isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(((ExtendedLongCounter) meter.counterBuilder("longCounter").build()).isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(
            ((ExtendedDoubleUpDownCounter)
                    meter.upDownCounterBuilder("doubleUpDownCounter").ofDoubles().build())
                .isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(
            ((ExtendedLongUpDownCounter) meter.upDownCounterBuilder("longUpDownCounter").build())
                .isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(
            ((ExtendedDoubleHistogram) meter.histogramBuilder("doubleHistogram").build())
                .isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(
            ((ExtendedLongHistogram) meter.histogramBuilder("longHistogram").ofLongs().build())
                .isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(((ExtendedDoubleGauge) meter.gaugeBuilder("doubleGauge").build()).isEnabled())
        .isEqualTo(expectedEnabled);
    assertThat(((ExtendedLongGauge) meter.gaugeBuilder("longGauge").ofLongs().build()).isEnabled())
        .isEqualTo(expectedEnabled);
  }

  @ParameterizedTest
  @MethodSource("meterConfiguratorArgs")
  void meterConfigurator(
      ScopeConfigurator<MeterConfig> meterConfigurator,
      InstrumentationScopeInfo scope,
      MeterConfig expectedMeterConfig) {
    MeterConfig meterConfig = meterConfigurator.apply(scope);
    meterConfig = meterConfig == null ? defaultConfig() : meterConfig;
    assertThat(meterConfig).isEqualTo(expectedMeterConfig);
  }

  private static final InstrumentationScopeInfo scopeCat = InstrumentationScopeInfo.create("cat");
  private static final InstrumentationScopeInfo scopeDog = InstrumentationScopeInfo.create("dog");
  private static final InstrumentationScopeInfo scopeDuck = InstrumentationScopeInfo.create("duck");

  private static Stream<Arguments> meterConfiguratorArgs() {
    ScopeConfigurator<MeterConfig> defaultConfigurator = MeterConfig.configuratorBuilder().build();
    ScopeConfigurator<MeterConfig> disableCat =
        MeterConfig.configuratorBuilder()
            .addCondition(nameEquals("cat"), MeterConfig.disabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), enabled())
            .build();
    ScopeConfigurator<MeterConfig> disableStartsWithD =
        MeterConfig.configuratorBuilder()
            .addCondition(nameMatchesGlob("d*"), MeterConfig.disabled())
            .build();
    ScopeConfigurator<MeterConfig> enableCat =
        MeterConfig.configuratorBuilder()
            .setDefault(MeterConfig.disabled())
            .addCondition(nameEquals("cat"), enabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), MeterConfig.disabled())
            .build();
    ScopeConfigurator<MeterConfig> enableStartsWithD =
        MeterConfig.configuratorBuilder()
            .setDefault(MeterConfig.disabled())
            .addCondition(nameMatchesGlob("d*"), MeterConfig.enabled())
            .build();

    return Stream.of(
        // default
        Arguments.of(defaultConfigurator, scopeCat, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDog, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDuck, defaultConfig()),
        // default enabled, disable cat
        Arguments.of(disableCat, scopeCat, MeterConfig.disabled()),
        Arguments.of(disableCat, scopeDog, enabled()),
        Arguments.of(disableCat, scopeDuck, enabled()),
        // default enabled, disable pattern
        Arguments.of(disableStartsWithD, scopeCat, enabled()),
        Arguments.of(disableStartsWithD, scopeDog, MeterConfig.disabled()),
        Arguments.of(disableStartsWithD, scopeDuck, MeterConfig.disabled()),
        // default disabled, enable cat
        Arguments.of(enableCat, scopeCat, enabled()),
        Arguments.of(enableCat, scopeDog, MeterConfig.disabled()),
        Arguments.of(enableCat, scopeDuck, MeterConfig.disabled()),
        // default disabled, enable pattern
        Arguments.of(enableStartsWithD, scopeCat, MeterConfig.disabled()),
        Arguments.of(enableStartsWithD, scopeDog, enabled()),
        Arguments.of(enableStartsWithD, scopeDuck, enabled()));
  }

  @Test
  void setScopeConfigurator() {
    // 1. Initially, configure all meters to be enabled except meterB
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .addMeterConfiguratorCondition(nameEquals("meterB"), disabled())
            .registerMetricReader(reader)
            .build();

    SdkMeter meterA = (SdkMeter) meterProvider.get("meterA");
    SdkMeter meterB = (SdkMeter) meterProvider.get("meterB");
    SdkMeter meterC = (SdkMeter) meterProvider.get("meterC");

    // verify isMeterEnabled()
    assertThat(meterA.isMeterEnabled()).isTrue();
    assertThat(meterB.isMeterEnabled()).isFalse();
    assertThat(meterC.isMeterEnabled()).isTrue();

    // verify metrics are emitted as expected
    meterA.counterBuilder("meterA").build().add(1);
    meterB.counterBuilder("meterB").build().add(2);
    meterC.counterBuilder("meterC").build().add(3);
    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData -> assertThat(metricData).hasName("meterA"),
            metricData -> assertThat(metricData).hasName("meterC"));

    // 2. Update config to disable all meters
    meterProvider.setMeterConfigurator(
        ScopeConfigurator.<MeterConfig>builder().setDefault(disabled()).build());

    // verify isEnabled()
    assertThat(meterA.isMeterEnabled()).isFalse();
    assertThat(meterB.isMeterEnabled()).isFalse();
    assertThat(meterC.isMeterEnabled()).isFalse();

    // verify metrics are emitted as expected
    meterA.counterBuilder("meterA").build().add(1);
    meterB.counterBuilder("meterB").build().add(2);
    meterC.counterBuilder("meterC").build().add(3);
    assertThat(reader.collectAllMetrics()).isEmpty();

    // 3. Update config to restore original
    meterProvider.setMeterConfigurator(
        ScopeConfigurator.<MeterConfig>builder()
            .addCondition(nameEquals("meterB"), disabled())
            .build());

    // verify isEnabled()
    assertThat(meterA.isMeterEnabled()).isTrue();
    assertThat(meterB.isMeterEnabled()).isFalse();
    assertThat(meterC.isMeterEnabled()).isTrue();

    // verify metrics are emitted as expected
    meterA.counterBuilder("meterA").build().add(1);
    meterB.counterBuilder("meterB").build().add(2);
    meterC.counterBuilder("meterC").build().add(3);
    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData -> assertThat(metricData).hasName("meterA"),
            metricData -> assertThat(metricData).hasName("meterC"));
  }
}
