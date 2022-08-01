/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static java.util.stream.Collectors.groupingBy;

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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class OpenTelemetryMetricExporterTest {

  @Test
  @SuppressWarnings({"deprecation"}) // Summary is deprecated in census
  void testSupportedMetricsExportedCorrectly() {
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
                          // Filter for metrics with name in allowedMetrics, and dedupe to only one
                          // metric per unique metric name
                          exporter.getFinishedMetricItems().stream()
                              .filter(metric -> allowedMetrics.contains(metric.getName()))
                              .collect(groupingBy(MetricData::getName))
                              .values()
                              .stream()
                              .map(metricData -> metricData.get(0))
                              .collect(Collectors.toList()))
                      .satisfiesExactlyInAnyOrder(
                          metric ->
                              assertThat(metric)
                                  .hasName("double_gauge")
                                  .hasDescription("double gauge")
                                  .hasUnit("ms")
                                  .hasDoubleGaugeSatisfying(
                                      gauge ->
                                          gauge.hasPointsSatisfying(
                                              point ->
                                                  point
                                                      .hasValue(60)
                                                      .hasAttributes(Attributes.empty()))),
                          metric ->
                              assertThat(metric)
                                  .hasName("double_sum")
                                  .hasDescription("double sum")
                                  .hasUnit("ms")
                                  .hasDoubleSumSatisfying(
                                      sum ->
                                          sum.hasPointsSatisfying(
                                              point ->
                                                  point
                                                      .hasValue(60)
                                                      .hasAttributes(Attributes.empty()))),
                          metric ->
                              assertThat(metric)
                                  .hasName("long_gauge")
                                  .hasDescription("long gauge")
                                  .hasUnit("ms")
                                  .hasLongGaugeSatisfying(
                                      gauge ->
                                          gauge.hasPointsSatisfying(
                                              point ->
                                                  point
                                                      .hasValue(50)
                                                      .hasAttributes(
                                                          attributeEntry(
                                                              tagKey.getName(),
                                                              tagValue.asString())))),
                          metric ->
                              assertThat(metric)
                                  .hasName("long_sum")
                                  .hasDescription("long sum")
                                  .hasUnit("ms")
                                  .hasLongSumSatisfying(
                                      sum ->
                                          sum.hasPointsSatisfying(
                                              point ->
                                                  point
                                                      .hasValue(50)
                                                      .hasAttributes(
                                                          attributeEntry(
                                                              tagKey.getName(),
                                                              tagValue.asString()))))));
    } finally {
      otelExporter.stop();
    }
  }
}
