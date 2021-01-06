/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opencensus.common.Duration;
import io.opencensus.common.Scope;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetricInteroperabilityTest {

  @Test
  @SuppressWarnings("deprecation") // Summary is deprecated in census
  public void testSupportedMetricsExportedCorrectly() {
    Tagger tagger = Tags.getTagger();
    MeasureLong latency =
        MeasureLong.create("task_latency", "The task latency in milliseconds", "ms");
    MeasureDouble latency2 =
        MeasureDouble.create("task_latency_2", "The task latency in milliseconds 2", "ms");
    StatsRecorder statsRecorder = Stats.getStatsRecorder();
    TagKey tagKey = TagKey.create("tagKey");
    TagValue tagValue = TagValue.create("tagValue");
    View longSumView =
        View.create(
            Name.create("long_sum"),
            "long sum",
            latency,
            Aggregation.Sum.create(),
            ImmutableList.of(tagKey));
    View longGaugeView =
        View.create(
            Name.create("long_gauge"),
            "long gauge",
            latency,
            Aggregation.LastValue.create(),
            ImmutableList.of(tagKey));
    View doubleSumView =
        View.create(
            Name.create("double_sum"),
            "double sum",
            latency2,
            Aggregation.Sum.create(),
            ImmutableList.of());
    View doubleGaugeView =
        View.create(
            Name.create("double_gauge"),
            "double gauge",
            latency2,
            Aggregation.LastValue.create(),
            ImmutableList.of());
    ViewManager viewManager = Stats.getViewManager();
    viewManager.registerView(longSumView);
    viewManager.registerView(longGaugeView);
    viewManager.registerView(doubleSumView);
    viewManager.registerView(doubleGaugeView);
    FakeMetricExporter metricExporter = new FakeMetricExporter();
    OpenTelemetryMetricsExporter.createAndRegister(metricExporter, Duration.create(0, 5000));

    TagContext tagContext =
        tagger
            .emptyBuilder()
            .put(tagKey, tagValue, TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))
            .build();
    try (Scope ss = tagger.withTagContext(tagContext)) {
      statsRecorder.newMeasureMap().put(latency, 50).record();
      statsRecorder.newMeasureMap().put(latency2, 60).record();
    }
    List<List<MetricData>> exported = metricExporter.waitForNumberOfExports(3);
    List<MetricData> metricData = exported.get(2);
    assertThat(metricData.size()).isEqualTo(4);

    MetricData metric = metricData.get(0);
    assertThat(metric.getName()).isEqualTo("long_sum");
    assertThat(metric.getDescription()).isEqualTo("long sum");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metric.getLongSumData().getPoints().size()).isEqualTo(1);
    Point point = metric.getLongSumData().getPoints().iterator().next();
    assertThat(((LongPoint) point).getValue()).isEqualTo(50);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get(tagKey.getName())).isEqualTo(tagValue.asString());

    metric = metricData.get(1);
    assertThat(metric.getName()).isEqualTo("long_gauge");
    assertThat(metric.getDescription()).isEqualTo("long gauge");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.LONG_GAUGE);
    assertThat(metric.getLongGaugeData().getPoints().size()).isEqualTo(1);

    metric = metricData.get(2);
    assertThat(metric.getName()).isEqualTo("double_sum");
    assertThat(metric.getDescription()).isEqualTo("double sum");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.DOUBLE_SUM);
    assertThat(metric.getDoubleSumData().getPoints().size()).isEqualTo(1);
    point = metric.getDoubleSumData().getPoints().iterator().next();
    assertThat(((DoublePoint) point).getValue()).isEqualTo(60);
    assertThat(point.getLabels().size()).isEqualTo(0);

    metric = metricData.get(3);
    assertThat(metric.getName()).isEqualTo("double_gauge");
    assertThat(metric.getDescription()).isEqualTo("double gauge");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.DOUBLE_GAUGE);
    assertThat(metric.getDoubleGaugeData().getPoints().size()).isEqualTo(1);
    point = metric.getDoubleGaugeData().getPoints().iterator().next();
    assertThat(((DoublePoint) point).getValue()).isEqualTo(60);
    assertThat(point.getLabels().size()).isEqualTo(0);
  }

  @Test
  public void testUnsupportedMetricsDoesNotGetExported() throws InterruptedException {
    Tagger tagger = Tags.getTagger();
    MeasureLong latency =
        MeasureLong.create("task_latency_distribution", "The task latency in milliseconds", "ms");
    StatsRecorder statsRecorder = Stats.getStatsRecorder();
    TagKey tagKey = TagKey.create("tagKey");
    TagValue tagValue = TagValue.create("tagValue");
    View view =
        View.create(
            Name.create("task_latency_distribution"),
            "The distribution of the task latencies.",
            latency,
            Aggregation.Distribution.create(
                BucketBoundaries.create(ImmutableList.of(100.0, 150.0, 200.0))),
            ImmutableList.of(tagKey));
    ViewManager viewManager = Stats.getViewManager();
    viewManager.registerView(view);
    FakeMetricExporter metricExporter = new FakeMetricExporter();
    OpenTelemetryMetricsExporter.createAndRegister(metricExporter, Duration.create(0, 500));

    TagContext tagContext =
        tagger
            .emptyBuilder()
            .put(tagKey, tagValue, TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))
            .build();
    try (Scope ss = tagger.withTagContext(tagContext)) {
      statsRecorder.newMeasureMap().put(latency, 50).record();
    }
    // Sleep so that there is time for export() to be called.
    Thread.sleep(2);
    // This is 0 in case this test gets run first, or by itself.
    // If other views have already been registered in other tests, they will produce metric data, so
    // we are testing for the absence of this particular view's metric data.
    List<List<MetricData>> allExports = metricExporter.waitForNumberOfExports(0);
    if (!allExports.isEmpty()) {
      for (MetricData metricData : allExports.get(allExports.size() - 1)) {
        assertThat(metricData.getName()).isNotEqualTo("task_latency_distribution");
      }
    }
  }
}
