/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class OpenTelemetryMetricExporterTest {

  @Test
  @SuppressWarnings({"deprecation", "unchecked"}) // Summary is deprecated in census
  void testSupportedMetricsExportedCorrectly() throws Exception {
    Tagger tagger = Tags.getTagger();
    Measure.MeasureLong latency =
        Measure.MeasureLong.create("task_latency", "The task latency in milliseconds", "ms");
    Measure.MeasureDouble latency2 =
        Measure.MeasureDouble.create("task_latency_2", "The task latency in milliseconds 2", "ms");
    StatsRecorder statsRecorder = Stats.getStatsRecorder();
    TagKey tagKey = TagKey.create("tagKey");
    TagValue tagValue = TagValue.create("tagValue");
    View longSumView =
        View.create(
            View.Name.create("long_sum"),
            "long sum",
            latency,
            Aggregation.Sum.create(),
            ImmutableList.of(tagKey));
    View longGaugeView =
        View.create(
            View.Name.create("long_gauge"),
            "long gauge",
            latency,
            Aggregation.LastValue.create(),
            ImmutableList.of(tagKey));
    View doubleSumView =
        View.create(
            View.Name.create("double_sum"),
            "double sum",
            latency2,
            Aggregation.Sum.create(),
            ImmutableList.of());
    View doubleGaugeView =
        View.create(
            View.Name.create("double_gauge"),
            "double gauge",
            latency2,
            Aggregation.LastValue.create(),
            ImmutableList.of());
    ViewManager viewManager = Stats.getViewManager();
    viewManager.registerView(longSumView);
    viewManager.registerView(longGaugeView);
    viewManager.registerView(doubleSumView);
    viewManager.registerView(doubleGaugeView);
    // Create OpenCensus -> OpenTelemetry Exporter bridge
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    OpenTelemetryMetricsExporter otelExporter =
        OpenTelemetryMetricsExporter.createAndRegister(exporter, Duration.create(1, 0));
    try {
      TagContext tagContext =
          tagger
              .emptyBuilder()
              .put(tagKey, tagValue, TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION))
              .build();
      try (io.opencensus.common.Scope ss = tagger.withTagContext(tagContext)) {
        statsRecorder.newMeasureMap().put(latency, 50).record();
        statsRecorder.newMeasureMap().put(latency2, 60).record();
      }
      Set<String> allowedMetrics = new HashSet<>();
      allowedMetrics.add("double_gauge");
      allowedMetrics.add("double_sum");
      allowedMetrics.add("long_gauge");
      allowedMetrics.add("long_sum");
      // Slow down for OpenCensus to catch up.
      Awaitility.await()
          .atMost(java.time.Duration.ofSeconds(10))
          .untilAsserted(
              () ->
                  assertThat(
                          exporter.getFinishedMetricItems().stream()
                              .filter(metric -> allowedMetrics.contains(metric.getName()))
                              .sorted(Comparator.comparing(MetricData::getName))
                              .collect(Collectors.toList()))
                      .satisfiesExactly(
                          metric ->
                              assertThat(metric)
                                  .hasName("double_gauge")
                                  .hasDescription("double gauge")
                                  .hasUnit("ms")
                                  .hasDoubleGauge()
                                  .points()
                                  .satisfiesExactly(
                                      point ->
                                          assertThat(point).hasValue(60).attributes().hasSize(0)),
                          metric ->
                              assertThat(metric)
                                  .hasName("double_sum")
                                  .hasDescription("double sum")
                                  .hasUnit("ms")
                                  .hasDoubleSum()
                                  .points()
                                  .satisfiesExactly(
                                      point ->
                                          assertThat(point).hasValue(60).attributes().hasSize(0)),
                          metric ->
                              assertThat(metric)
                                  .hasName("long_gauge")
                                  .hasDescription("long gauge")
                                  .hasUnit("ms")
                                  .hasLongGauge()
                                  .points()
                                  .satisfiesExactly(
                                      point ->
                                          assertThat(point)
                                              .hasValue(50)
                                              .attributes()
                                              .hasSize(1)
                                              .containsEntry(
                                                  tagKey.getName(), tagValue.asString())),
                          metric ->
                              assertThat(metric)
                                  .hasName("long_sum")
                                  .hasDescription("long sum")
                                  .hasUnit("ms")
                                  .hasLongSum()
                                  .points()
                                  .satisfiesExactly(
                                      point ->
                                          assertThat(point)
                                              .hasValue(50)
                                              .attributes()
                                              .hasSize(1)
                                              .containsEntry(
                                                  tagKey.getName(), tagValue.asString()))));
    } finally {
      otelExporter.stop();
    }
  }
}
