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
    ViewManager viewManager = Stats.getViewManager();
    viewManager.registerView(view);
    FakeMetricExporter metricExporter = new FakeMetricExporter();
    OpenTelemetryMetricsExporter exporter =
        OpenTelemetryMetricsExporter.createAndRegister(metricExporter, Duration.create(0, 100));

    TagContext tagContext =
        tagger
            .emptyBuilder()
            .put(tagKey, tagValue, TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))
            .build();
    try (Scope ss = tagger.withTagContext(tagContext)) {
      statsRecorder.newMeasureMap().put(latency, 50).record();
    }
    exporter.stop();
    List<MetricData> metricData = metricExporter.waitForNumberOfExports(1).get(0);
    assertThat(metricData.size()).isEqualTo(1);
    MetricData metric = metricData.get(0);
    assertThat(metric.getName()).isEqualTo("task_latency_sum");
    assertThat(metric.getDescription()).isEqualTo("The sum of the task latencies.");
    assertThat(metric.getUnit()).isEqualTo("ms");
    assertThat(metric.getType()).isEqualTo(MetricData.Type.MONOTONIC_LONG);
    assertThat(metric.getPoints().size()).isEqualTo(1);
    Point point = metric.getPoints().iterator().next();
    assertThat(((LongPoint) point).getValue()).isEqualTo(50);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get(tagKey.getName())).isEqualTo(tagValue.asString());
  }
}
