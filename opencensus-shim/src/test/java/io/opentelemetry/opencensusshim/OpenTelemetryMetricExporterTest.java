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
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
    WaitingMetricExporter exporter = new WaitingMetricExporter();
    OpenTelemetryMetricsExporter otelExporter =
        OpenTelemetryMetricsExporter.createAndRegister(exporter, Duration.create(0, 5000));
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
      // Slow down for OpenCensus to catch up.
      List<List<MetricData>> result = exporter.waitForNumberOfExports(3);
      // Just look at last export.
      List<MetricData> metricData =
          result.get(2).stream()
              .sorted(Comparator.comparing(MetricData::getName))
              .collect(Collectors.toList());
      assertThat(metricData.size()).isEqualTo(4);

      MetricData metric = metricData.get(0);
      MetricAssertions.assertThat(metric)
          .hasName("double_gauge")
          .hasDescription("double gauge")
          .hasUnit("ms")
          .hasDoubleGauge()
          .points()
          .satisfiesExactly(
              point -> MetricAssertions.assertThat(point).hasValue(60).attributes().hasSize(0));
      metric = metricData.get(1);
      MetricAssertions.assertThat(metric)
          .hasName("double_sum")
          .hasDescription("double sum")
          .hasUnit("ms")
          .hasDoubleSum()
          .points()
          .satisfiesExactly(
              point -> MetricAssertions.assertThat(point).hasValue(60).attributes().hasSize(0));
      metric = metricData.get(2);
      MetricAssertions.assertThat(metric)
          .hasName("long_gauge")
          .hasDescription("long gauge")
          .hasUnit("ms")
          .hasLongGauge()
          .points()
          .satisfiesExactly(
              point ->
                  MetricAssertions.assertThat(point)
                      .hasValue(50)
                      .attributes()
                      .hasSize(1)
                      .containsEntry(tagKey.getName(), tagValue.asString()));
      metric = metricData.get(3);
      MetricAssertions.assertThat(metric)
          .hasName("long_sum")
          .hasDescription("long sum")
          .hasUnit("ms")
          .hasLongSum()
          .points()
          .satisfiesExactly(
              point ->
                  MetricAssertions.assertThat(point)
                      .hasValue(50)
                      .attributes()
                      .hasSize(1)
                      .containsEntry(tagKey.getName(), tagValue.asString()));
    } finally {
      otelExporter.stop();
    }
  }

  // Straight copy-paste from PeriodicMetricReaderTest.  Should likely move into metrics-testing.
  private static class WaitingMetricExporter implements MetricExporter {

    private final AtomicBoolean hasShutdown = new AtomicBoolean(false);
    private final boolean shouldThrow;
    private final BlockingQueue<List<MetricData>> queue = new LinkedBlockingQueue<>();
    private final List<Long> exportTimes = Collections.synchronizedList(new ArrayList<>());

    private WaitingMetricExporter() {
      this(false);
    }

    private WaitingMetricExporter(boolean shouldThrow) {
      this.shouldThrow = shouldThrow;
    }

    @Override
    public EnumSet<AggregationTemporality> getSupportedTemporality() {
      return EnumSet.allOf(AggregationTemporality.class);
    }

    @Override
    public AggregationTemporality getPreferredTemporality() {
      return null;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metricList) {
      exportTimes.add(System.currentTimeMillis());
      queue.offer(new ArrayList<>(metricList));

      if (shouldThrow) {
        throw new RuntimeException("Export Failed!");
      }
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      hasShutdown.set(true);
      return CompletableResultCode.ofSuccess();
    }

    /**
     * Waits until export is called for numberOfExports times. Returns the list of exported lists of
     * metrics.
     */
    @Nullable
    List<List<MetricData>> waitForNumberOfExports(int numberOfExports) throws Exception {
      List<List<MetricData>> result = new ArrayList<>();
      while (result.size() < numberOfExports) {
        List<MetricData> export = queue.poll(5, TimeUnit.SECONDS);
        assertThat(export).isNotNull();
        result.add(export);
      }
      return result;
    }
  }
}
