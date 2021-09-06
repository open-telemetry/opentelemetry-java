/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
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
public class DoubleHistogramAggregatorTest {

  @Mock ExemplarReservoir reservoir;

  private static final double[] boundaries = new double[] {10.0, 100.0, 1000.0};
  private static final DoubleHistogramAggregator aggregator =
      new DoubleHistogramAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          MetricDescriptor.create("name", "description", "unit"),
          boundaries,
          /* stateful= */ false,
          ExemplarReservoir::empty);

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
        .isEqualTo(HistogramAccumulation.create(2175, new long[] {1, 1, 1, 1}));
  }

  @Test
  void testExemplarsInAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Exemplar exemplar = DoubleExemplar.create(attributes, 2L, "spanid", "traceid", 1);
    List<Exemplar> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);
    DoubleHistogramAggregator aggregator =
        new DoubleHistogramAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            MetricDescriptor.create("name", "description", "unit"),
            boundaries,
            /* stateful= */ false,
            () -> reservoir);
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(HistogramAccumulation.create(0, new long[] {1, 0, 0, 0}, exemplars));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(100);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(HistogramAccumulation.create(100, new long[] {0, 1, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(0);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(HistogramAccumulation.create(0, new long[] {1, 0, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void accumulateData() {
    assertThat(aggregator.accumulateDouble(11.1))
        .isEqualTo(HistogramAccumulation.create(11.1, new long[] {0, 1, 0, 0}));
    assertThat(aggregator.accumulateLong(10))
        .isEqualTo(HistogramAccumulation.create(10.0, new long[] {1, 0, 0, 0}));
  }

  @Test
  void toMetricData() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
            0,
            10,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(metricData.getDoubleHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void toMetricDataWithExemplars() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Exemplar exemplar = DoubleExemplar.create(attributes, 2L, "spanid", "traceid", 1);
    HistogramAccumulation accumulation =
        HistogramAccumulation.create(
            2, new long[] {1, 0, 0, 0}, Collections.singletonList(exemplar));
    assertThat(
            aggregator.toMetricData(
                Collections.singletonMap(Attributes.empty(), accumulation), 0, 10, 100))
        .hasDoubleHistogram()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasSum(2)
                    .hasBucketCounts(1, 0, 0, 0)
                    .hasCount(1)
                    .hasExemplars(exemplar));
  }

  @Test
  void testHistogramCounts() {
    assertThat(aggregator.accumulateDouble(1.1).getCounts().length)
        .isEqualTo(boundaries.length + 1);
    assertThat(aggregator.accumulateLong(1).getCounts().length).isEqualTo(boundaries.length + 1);

    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(1.1);
    HistogramAccumulation histogramAccumulation =
        aggregatorHandle.accumulateThenReset(Attributes.empty());
    assertThat(histogramAccumulation).isNotNull();
    assertThat(histogramAccumulation.getCounts().length).isEqualTo(boundaries.length + 1);
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    final AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    final Histogram summarizer = new Histogram();
    final ImmutableList<Long> updates =
        ImmutableList.of(1L, 2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L);
    final int numberOfThreads = updates.size();
    final int numberOfUpdates = 10000;
    final ThreadPoolExecutor executor =
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
        .isEqualTo(HistogramAccumulation.create(1010000, new long[] {50000, 50000, 0, 0}));
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
