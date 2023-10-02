/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opentelemetry.opencensusshim.OpenCensusMetricProducer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import java.util.Collections;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class OpenCensusMetricsTest {
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();

  @Test
  void capturesOpenCensusAndOtelMetrics() throws InterruptedException {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider otelMetrics =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerMetricProducer(OpenCensusMetricProducer.create())
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
            () ->
                assertThat(reader.collectAllMetrics())
                    .satisfiesExactlyInAnyOrder(
                        metric ->
                            assertThat(metric).hasName("otel.sum").hasLongSumSatisfying(sum -> {}),
                        metric ->
                            assertThat(metric).hasName("oc.sum").hasLongSumSatisfying(sum -> {})));
  }
}
