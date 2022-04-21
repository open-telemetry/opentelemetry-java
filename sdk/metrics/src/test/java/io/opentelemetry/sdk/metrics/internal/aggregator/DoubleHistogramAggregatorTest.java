/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoubleHistogramAggregatorTest {

  @Mock
  DoubleExemplarReservoir reservoir;

  private static final double[] boundaries = new double[] {10.0, 100.0, 1000.0};
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final DoubleHistogramAggregator aggregator =
      new DoubleHistogramAggregator(boundaries, ExemplarReservoir::noSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleHistogramAggregator.Handle.class);
  }

  @Test
  void testRecordings() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(20);
    aggregatorHandle.recordLong(5);
    aggregatorHandle.recordLong(150);
    aggregatorHandle.recordLong(2000);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(
            HistogramAccumulation.create(
                2175, /* hasMinMax= */ true, 5d, 2000d, new long[] {1, 1, 1, 1}));
  }

  @Test
  void testExemplarsInAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);
    DoubleHistogramAggregator aggregator =
        new DoubleHistogramAggregator(boundaries, () -> reservoir);
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 0, 0, new long[] {1, 0, 0, 0}, exemplars));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(100);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(
            HistogramAccumulation.create(
                100, /* hasMinMax= */ true, 100d, 100d, new long[] {0, 1, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(0);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 0d, 0d, new long[] {1, 0, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void accumulateData() {
    assertThat(aggregator.accumulateDoubleMeasurement(11.1, Attributes.empty(), Context.current()))
        .isEqualTo(
            HistogramAccumulation.create(
                11.1, /* hasMinMax= */ true, 11.1, 11.1, new long[] {0, 1, 0, 0}));
    assertThat(aggregator.accumulateLongMeasurement(10, Attributes.empty(), Context.current()))
        .isEqualTo(
            HistogramAccumulation.create(
                10.0, /* hasMinMax= */ true, 10.0, 10.0, new long[] {1, 0, 0, 0}));
  }

  @Test
  void mergeAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    List<ExemplarData> previousExemplars =
        Collections.singletonList(
            ImmutableDoubleExemplarData.create(
                attributes,
                1L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                2));
    HistogramAccumulation previousAccumulation =
        HistogramAccumulation.create(
            2, /* hasMinMax= */ true, 1d, 2d, new long[] {1, 1, 0}, previousExemplars);
    HistogramAccumulation nextAccumulation =
        HistogramAccumulation.create(
            2, /* hasMinMax= */ true, 2d, 3d, new long[] {0, 0, 2}, exemplars);
    // Assure most recent exemplars are kept.
    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
        .isEqualTo(
            HistogramAccumulation.create(
                4, /* hasMinMax= */ true, 1d, 3d, new long[] {1, 1, 2}, exemplars));
  }

  @Test
  void mergeAccumulation_MinAndMax() {
    // If min / max is null for both accumulations set min / max to null
    assertThat(
            aggregator.merge(
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()),
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ false, 0, 0, new long[] {}, Collections.emptyList())))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()));
    // If min / max is non-null for only one accumulation set min / max to it
    assertThat(
            aggregator.merge(
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()),
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ false, 0, 0, new long[] {}, Collections.emptyList())))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()));
    assertThat(
            aggregator.merge(
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ false, 0, 0, new long[] {}, Collections.emptyList()),
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList())))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()));
    // If both accumulations have min / max compute the min / max
    assertThat(
            aggregator.merge(
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ true, 1d, 1d, new long[] {}, Collections.emptyList()),
                HistogramAccumulation.create(
                    0, /* hasMinMax= */ true, 2d, 2d, new long[] {}, Collections.emptyList())))
        .isEqualTo(
            HistogramAccumulation.create(
                0, /* hasMinMax= */ true, 1d, 2d, new long[] {}, Collections.emptyList()));
  }

  @Test
  void diffAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    List<ExemplarData> previousExemplars =
        Collections.singletonList(
            ImmutableDoubleExemplarData.create(
                attributes,
                1L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                2));
    HistogramAccumulation previousAccumulation =
        HistogramAccumulation.create(
            2, /* hasMinMax= */ true, 1d, 2d, new long[] {1, 1, 2}, previousExemplars);
    HistogramAccumulation nextAccumulation =
        HistogramAccumulation.create(
            5, /* hasMinMax= */ true, 2d, 3d, new long[] {2, 2, 2}, exemplars);
    // Assure most recent exemplars are kept.
    assertThat(aggregator.diff(previousAccumulation, nextAccumulation))
        .isEqualTo(
            HistogramAccumulation.create(
                3, /* hasMinMax= */ false, -1, -1, new long[] {1, 1, 0}, exemplars));
  }

  @Test
  void toMetricData() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
            AggregationTemporality.DELTA,
            0,
            10,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(metricData.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void toMetricDataWithExemplars() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    HistogramAccumulation accumulation =
        HistogramAccumulation.create(
            2,
            /* hasMinMax= */ true,
            2d,
            2d,
            new long[] {1, 0, 0, 0},
            Collections.singletonList(exemplar));
    assertThat(
            aggregator.toMetricData(
                RESOURCE,
                INSTRUMENTATION_SCOPE_INFO,
                METRIC_DESCRIPTOR,
                Collections.singletonMap(Attributes.empty(), accumulation),
                AggregationTemporality.CUMULATIVE,
                0,
                10,
                100))
        .hasDoubleHistogram()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasSum(2)
                    .hasMin(2)
                    .hasMax(2)
                    .hasBucketCounts(1, 0, 0, 0)
                    .hasCount(1)
                    .hasExemplars(exemplar));
  }

  @Test
  void testHistogramCounts() {
    assertThat(
            aggregator
                .accumulateDoubleMeasurement(1.1, Attributes.empty(), Context.root())
                .getCounts()
                .length)
        .isEqualTo(boundaries.length + 1);
    assertThat(
            aggregator
                .accumulateLongMeasurement(1, Attributes.empty(), Context.root())
                .getCounts()
                .length)
        .isEqualTo(boundaries.length + 1);

    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(1.1);
    HistogramAccumulation histogramAccumulation =
        aggregatorHandle.accumulateThenReset(Attributes.empty());
    assertThat(histogramAccumulation).isNotNull();
    assertThat(histogramAccumulation.getCounts().length).isEqualTo(boundaries.length + 1);
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    Histogram summarizer = new Histogram();
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
                              summarizer.process(
                                  aggregatorHandle.accumulateThenReset(Attributes.empty()));
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    // make sure everything gets merged when all the aggregation is done.
    summarizer.process(aggregatorHandle.accumulateThenReset(Attributes.empty()));

    assertThat(summarizer.accumulation)
        .isEqualTo(
            HistogramAccumulation.create(
                1010000, /* hasMinMax= */ true, 1d, 23d, new long[] {50000, 50000, 0, 0}));
  }

  private static final class Histogram {
    private final Object mutex = new Object();

    @Nullable private HistogramAccumulation accumulation;

    void process(@Nullable HistogramAccumulation other) {
      if (other == null) {
        return;
      }

      synchronized (mutex) {
        if (accumulation == null) {
          accumulation = other;
          return;
        }
        accumulation = aggregator.merge(accumulation, other);
      }
    }
  }
}
