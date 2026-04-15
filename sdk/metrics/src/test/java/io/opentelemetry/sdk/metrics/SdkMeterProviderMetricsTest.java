/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SdkMeterProviderMetricsTest {
  @Test
  void simple() {
    InMemoryMetricExporter metricExporter = InMemoryMetricExporter.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.create(metricExporter))
            .build()) {
      Meter meter = meterProvider.get("test");

      LongCounter counter = meter.counterBuilder("counter").build();

      counter.add(1);

      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      metricExporter.reset();
      // Export again to export the metric reader's metric.
      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);

      List<MetricData> metrics = metricExporter.getFinishedMetricItems();
      assertThat(metrics)
          .satisfiesExactlyInAnyOrder(
              m -> assertThat(m).hasName("counter"),
              m -> {
                assertThat(m)
                    .hasName("otel.sdk.metric_reader.collection.duration")
                    .hasHistogramSatisfying(
                        h ->
                            h.hasPointsSatisfying(
                                p ->
                                    p.hasCount(1)
                                        .hasAttributesSatisfying(
                                            equalTo(
                                                SemConvAttributes.OTEL_COMPONENT_TYPE,
                                                "periodic_metric_reader"),
                                            equalTo(
                                                SemConvAttributes.OTEL_COMPONENT_NAME,
                                                "periodic_metric_reader/0"))));
              });
    }
  }
}
