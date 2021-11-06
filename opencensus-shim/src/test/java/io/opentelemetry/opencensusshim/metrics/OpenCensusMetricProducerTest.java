/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OpenCensusMetricProducerTest {
  private final MetricProducer openCensusMetrics =
      OpenCensusMetricProducer.create(Resource.empty());

  private static final Measure.MeasureLong LATENCY_MS =
      Measure.MeasureLong.create("task_latency", "The task latency in milliseconds", "ms");
  // Latency in buckets:
  // [>=0ms, >=100ms, >=200ms, >=400ms, >=1s, >=2s, >=4s]
  private static final BucketBoundaries LATENCY_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(0d, 100d, 200d, 400d, 1000d, 2000d, 4000d));
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();

  @Test
  void extractHistogram() throws InterruptedException {
    View view =
        View.create(
            View.Name.create("task_latency_distribution"),
            "The distribution of the task latencies.",
            LATENCY_MS,
            Aggregation.Distribution.create(LATENCY_BOUNDARIES),
            Collections.emptyList());
    Stats.getViewManager().registerView(view);
    STATS_RECORDER.newMeasureMap().put(LATENCY_MS, 50).record();
    // Wait for measurement to hit the aggregator.
    Thread.sleep(1000);

    assertThat(openCensusMetrics.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("task_latency_distribution")
                    .hasDescription("The distribution of the task latencies.")
                    .hasUnit("ms")
                    .hasDoubleHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(50)
                                .hasCount(1)
                                .hasBucketCounts(1, 0, 0, 0, 0, 0, 0)
                                .hasBucketBoundaries(100d, 200d, 400d, 1000d, 2000d, 4000d)
                                .hasExemplars()));
  }
}
