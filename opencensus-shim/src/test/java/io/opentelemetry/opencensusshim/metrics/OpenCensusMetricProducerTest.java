/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opencensus.contrib.exemplar.util.ExemplarUtils;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.awaitility.Awaitility;
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

  // For Exemplar
  private static final Random RANDOM = new Random(1234);
  private static final TraceId TRACE_ID = TraceId.generateRandomId(RANDOM);
  private static final SpanId SPAN_ID = SpanId.generateRandomId(RANDOM);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, Tracestate.builder().build());

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

    MeasureMap recorder = STATS_RECORDER.newMeasureMap();
    ExemplarUtils.putSpanContextAttachments(recorder, SPAN_CONTEXT);
    recorder.put(LATENCY_MS, 50).record();
    // Wait for measurement to hit the aggregator.

    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () ->
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
                                            .hasBucketBoundaries(
                                                100d, 200d, 400d, 1000d, 2000d, 4000d)
                                            .exemplars()
                                            .satisfiesExactly(
                                                exemplar ->
                                                    assertThat(exemplar)
                                                        .hasFilteredAttributes(Attributes.empty())
                                                        .hasValue(50)
                                                        .hasTraceId(TRACE_ID.toLowerBase16())
                                                        .hasSpanId(SPAN_ID.toLowerBase16())))));
  }
}
