/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.common.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.common.ScopeConfiguratorBuilder.nameMatchesGlob;
import static io.opentelemetry.sdk.metrics.MeterConfig.defaultConfig;
import static io.opentelemetry.sdk.metrics.MeterConfig.disabled;
import static io.opentelemetry.sdk.metrics.MeterConfig.enabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.ScopeConfigurator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            .build();

    Meter meterA = meterProvider.get("meterA");
    Meter meterB = meterProvider.get("meterB");
    Meter meterC = meterProvider.get("meterC");

    meterA.counterBuilder("counterA").build().add(1);
    meterA.counterBuilder("asyncCounterA").buildWithCallback(observable -> observable.record(1));
    meterA.upDownCounterBuilder("upDownCounterA").build().add(1);
    meterA
        .upDownCounterBuilder("asyncUpDownCounterA")
        .buildWithCallback(observable -> observable.record(1));
    meterA.histogramBuilder("histogramA").build().record(1.0);
    meterA.gaugeBuilder("gaugeA").buildWithCallback(observable -> observable.record(1.0));

    meterB.counterBuilder("counterB").build().add(1);
    meterB.counterBuilder("asyncCounterB").buildWithCallback(observable -> observable.record(1));
    meterB.upDownCounterBuilder("upDownCounterB").build().add(1);
    meterB
        .upDownCounterBuilder("asyncUpDownCounterB")
        .buildWithCallback(observable -> observable.record(1));
    meterB.histogramBuilder("histogramB").build().record(1.0);
    meterB.gaugeBuilder("gaugeB").buildWithCallback(observable -> observable.record(1.0));

    meterC.counterBuilder("counterC").build().add(1);
    meterC.counterBuilder("asyncCounterC").buildWithCallback(observable -> observable.record(1));
    meterC.upDownCounterBuilder("upDownCounterC").build().add(1);
    meterC
        .upDownCounterBuilder("asyncUpDownCounterC")
        .buildWithCallback(observable -> observable.record(1));
    meterC.histogramBuilder("histogramC").build().record(1.0);
    meterC.gaugeBuilder("gaugeC").buildWithCallback(observable -> observable.record(1.0));

    // Only metrics from meterA and meterC should be seen
    assertThat(reader.collectAllMetrics())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<MetricData>> metricsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(MetricData::getInstrumentationScopeInfo));
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterA"))).hasSize(6);
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterB"))).isNull();
              assertThat(metricsByScope.get(InstrumentationScopeInfo.create("meterC"))).hasSize(6);
            });
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
}
