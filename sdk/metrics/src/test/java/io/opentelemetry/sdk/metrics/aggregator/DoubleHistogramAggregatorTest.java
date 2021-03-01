/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

public class DoubleHistogramAggregatorTest {
  private static final double[] boundaries = new double[] {10.0, 100.0, 1000.0};
  private static final DoubleHistogramAggregator aggregator =
      new DoubleHistogramAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "name",
              "description",
              "unit",
              InstrumentType.VALUE_RECORDER,
              InstrumentValueType.LONG),
          boundaries,
          /* stateful= */ false);

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
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(HistogramAccumulation.create(2175, new long[] {1, 1, 1, 1}));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(100);
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(HistogramAccumulation.create(100, new long[] {0, 1, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(0);
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(HistogramAccumulation.create(0, new long[] {1, 0, 0, 0}));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
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
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            10,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(metricData.getDoubleHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testHistogramCounts() {
    assertThat(aggregator.accumulateDouble(1.1).getCounts().length)
        .isEqualTo(boundaries.length + 1);
    assertThat(aggregator.accumulateLong(1).getCounts().length).isEqualTo(boundaries.length + 1);

    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(1.1);
    HistogramAccumulation histogramAccumulation = aggregatorHandle.accumulateThenReset();
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
                              summarizer.process(aggregatorHandle.accumulateThenReset());
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    // make sure everything gets merged when all the aggregation is done.
    summarizer.process(aggregatorHandle.accumulateThenReset());

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
