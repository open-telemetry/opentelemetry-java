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
  public void testSupportedMetricsExportedCorrectly() {
    Tagger tagger = Tags.getTagger();
    MeasureLong latency =
        MeasureLong.create("task_latency", "The task latency in milliseconds", "ms");
    MeasureDouble latency2 =
        MeasureDouble.create("task_latency_2", "The task latency in milliseconds 2", "ms");
    StatsRecorder statsRecorder = Stats.getStatsRecorder();
    TagKey tagKey = TagKey.create("tagKey");
    TagValue tagValue = TagValue.create("tagValue");
    View view =
        View.create(
            Name.create("task_latency_sum"),
            "The sum of the task latencies.",
            latency,
            Aggregation.Sum.create(),
            ImmutableList.of(tagKey));
    View view2 =
        View.create(
            Name.create("task_latency_sum_2"),
            "The sum of the task latencies 2.",
            latency2,
            Aggregation.LastValue.create(),
            ImmutableList.of());
    ViewManager viewManager = Stats.getViewManager();
    viewManager.registerView(view);
    viewManager.registerView(view2);
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
    List<MetricData> metricData = metricExporter.waitForNumberOfExports(3).get(2);
    assertThat(metricData.size()).isEqualTo(2);

    MetricData metric = metricData.get(0);
    assertThat(metric.getName()).isEqualTo("task_latency_sum");
    assertThat(metric.getDescription()).isEqualTo("The sum of the task latencies.");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metric.getLongSumData().getPoints().size()).isEqualTo(1);
    Point point = metric.getLongSumData().getPoints().iterator().next();
    assertThat(((LongPoint) point).getValue()).isEqualTo(50);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get(tagKey.getName())).isEqualTo(tagValue.asString());

    MetricData metric2 = metricData.get(1);
    assertThat(metric2.getName()).isEqualTo("task_latency_sum_2");
    assertThat(metric2.getDescription()).isEqualTo("The sum of the task latencies 2.");
    assertThat(metric2.getUnit()).isEqualTo("ms");
    assertThat(metric2.getType()).isEqualTo(MetricData.Type.DOUBLE_GAUGE);
    assertThat(metric2.getPoints().size()).isEqualTo(1);
    Point point2 = metric2.getPoints().iterator().next();
    assertThat(((DoublePoint) point2).getValue()).isEqualTo(60);
    assertThat(point2.getLabels().size()).isEqualTo(0);
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
