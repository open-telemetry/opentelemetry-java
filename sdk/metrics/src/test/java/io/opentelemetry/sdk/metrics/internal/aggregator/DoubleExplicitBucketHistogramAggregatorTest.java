/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoubleExplicitBucketHistogramAggregatorTest {

  @Mock ExemplarReservoir<DoubleExemplarData> reservoir;

  private static final double[] boundaries = new double[] {10.0, 100.0, 1000.0};
  private static final List<Double> boundariesList =
      DoubleStream.of(boundaries).boxed().collect(Collectors.toList());
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final DoubleExplicitBucketHistogramAggregator aggregator =
      new DoubleExplicitBucketHistogramAggregator(boundaries, ExemplarReservoir::doubleNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle())
        .isInstanceOf(DoubleExplicitBucketHistogramAggregator.Handle.class);
  }

  @Test
  void testRecordings() {
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(20);
    aggregatorHandle.recordLong(5);
    aggregatorHandle.recordLong(150);
    aggregatorHandle.recordLong(2000);
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(
            ImmutableHistogramPointData.create(
                0,
                1,
                Attributes.empty(),
                2175,
                /* hasMin= */ true,
                5d,
                /* hasMax= */ true,
                2000d,
                boundariesList,
                Arrays.asList(1L, 1L, 1L, 1L)));
  }

  @Test
  void aggregateThenMaybeReset_WithExemplars() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    DoubleExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<DoubleExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);
    DoubleExplicitBucketHistogramAggregator aggregator =
        new DoubleExplicitBucketHistogramAggregator(boundaries, () -> reservoir);
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(
            ImmutableHistogramPointData.create(
                0,
                1,
                Attributes.empty(),
                0,
                /* hasMin= */ true,
                0.0,
                /* hasMax= */ true,
                0.0,
                boundariesList,
                Arrays.asList(1L, 0L, 0L, 0L),
                exemplars));
  }

  @Test
  void aggregateThenMaybeReset() {
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();

    aggregatorHandle.recordLong(100);
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(
            ImmutableHistogramPointData.create(
                0,
                1,
                Attributes.empty(),
                100,
                /* hasMin= */ true,
                100d,
                /* hasMax= */ true,
                100d,
                boundariesList,
                Arrays.asList(0L, 1L, 0L, 0L)));

    aggregatorHandle.recordLong(0);
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(
            ImmutableHistogramPointData.create(
                0,
                1,
                Attributes.empty(),
                0,
                /* hasMin= */ true,
                0d,
                /* hasMax= */ true,
                0d,
                boundariesList,
                Arrays.asList(1L, 0L, 0L, 0L)));
  }

  @Test
  void toMetricData() {
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    0, 1, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.DELTA);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(metricData.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void toMetricDataWithExemplars() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    DoubleExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    HistogramPointData histPoint =
        ImmutableHistogramPointData.create(
            0,
            1,
            Attributes.empty(),
            2,
            /* hasMin= */ true,
            2d,
            /* hasMax= */ true,
            2d,
            boundariesList,
            Arrays.asList(1L, 0L, 0L, 0L),
            Collections.singletonList(exemplar));
    assertThat(
            aggregator.toMetricData(
                RESOURCE,
                INSTRUMENTATION_SCOPE_INFO,
                METRIC_DESCRIPTOR,
                Collections.singletonList(histPoint),
                AggregationTemporality.CUMULATIVE))
        .hasHistogramSatisfying(
            histogram ->
                histogram.hasPointsSatisfying(
                    point ->
                        point
                            .hasSum(2)
                            .hasMin(2)
                            .hasMax(2)
                            .hasBucketCounts(1, 0, 0, 0)
                            .hasCount(1)
                            .hasExemplars(exemplar)));
  }

  @Test
  void testHistogramCounts() {
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(1.1);
    HistogramPointData point =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(point).isNotNull();
    assertThat(point.getCounts().size()).isEqualTo(boundaries.length + 1);
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    AggregatorHandle<HistogramPointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    ImmutableList<Long> updates = ImmutableList.of(1L, 2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L);
    int numberOfThreads = updates.size();
    int numberOfUpdates = 10000;
    ThreadPoolExecutor executor =
        (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

    executor.invokeAll(
        updates.stream()
            .map(
                v ->
                    Executors.callable(
                        () -> {
                          for (int j = 0; j < numberOfUpdates; j++) {
                            aggregatorHandle.recordLong(v);
                            if (ThreadLocalRandom.current().nextInt(10) == 0) {
                              aggregatorHandle.aggregateThenMaybeReset(
                                  0, 1, Attributes.empty(), /* reset= */ false);
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false))
        .isEqualTo(
            ImmutableHistogramPointData.create(
                0,
                1,
                Attributes.empty(),
                1010000,
                /* hasMin= */ true,
                1d,
                /* hasMax= */ true,
                23d,
                boundariesList,
                Arrays.asList(50000L, 50000L, 0L, 0L)));
  }
}
