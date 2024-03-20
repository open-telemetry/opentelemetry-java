/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.common.ScopeSelector.named;
import static io.opentelemetry.sdk.metrics.MeterConfig.disabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MeterConfigTest {

  @Test
  void disableScopes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            // Disable meterB. Since meters are enabled by default, meterA and meterC are enabled.
            .addScopeConfig(named("meterB"), disabled())
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
}
