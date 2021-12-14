/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class OpenCensusMetricsTest {
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();

  @Test
  void capturesOpenCensusAndOtelMetrics() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider otelMetrics =
        SdkMeterProvider.builder()
            .registerMetricReader(
                OpenCensusMetrics.attachTo(PeriodicMetricReader.newMetricReaderFactory(exporter)))
            .build();
    // Record an otel metric.
    otelMetrics.meterBuilder("otel").build().counterBuilder("otel.sum").build().add(1);
    // Record an OpenCensus metric.
    Measure.MeasureLong measure = Measure.MeasureLong.create("oc.measure", "oc.desc", "oc.unit");
    Stats.getViewManager()
        .registerView(
            View.create(
                View.Name.create("oc.sum"),
                "oc.desc",
                measure,
                Aggregation.Count.create(),
                Collections.emptyList()));
    STATS_RECORDER.newMeasureMap().put(measure, 1).record();

    // Wait for OpenCensus propagation.
    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              otelMetrics.forceFlush().join(10, TimeUnit.MILLISECONDS);
              assertThat(exporter.getFinishedMetricItems())
                  .satisfiesExactly(
                      metric -> assertThat(metric).hasName("otel.sum").hasLongSum(),
                      metric -> assertThat(metric).hasName("oc.sum").hasLongSum());
            });
  }
}
