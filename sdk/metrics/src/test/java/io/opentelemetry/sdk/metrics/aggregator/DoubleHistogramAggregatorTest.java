/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

public class DoubleHistogramAggregatorTest {
  private static final ImmutableList<Double> BUCKET_BOUNDARIES =
      ImmutableList.of(10.0, 100.0, 1000.0);
  private static final DoubleHistogramAggregator aggregator =
      new DoubleHistogramAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.getEmpty(),
          InstrumentDescriptor.create(
              "name",
              "description",
              "unit",
              InstrumentType.VALUE_RECORDER,
              InstrumentValueType.LONG),
          BUCKET_BOUNDARIES.stream().mapToDouble(i -> i).toArray(),
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
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(
            HistogramAccumulation.create(
                3, 175, BUCKET_BOUNDARIES, ImmutableList.of(1L, 1L, 1L, 0L)));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(100);
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(
            HistogramAccumulation.create(
                1, 100, BUCKET_BOUNDARIES, ImmutableList.of(0L, 0L, 1L, 0L)));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(0);
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(
            HistogramAccumulation.create(
                1, 0, BUCKET_BOUNDARIES, ImmutableList.of(1L, 0L, 0L, 0L)));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
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
  void accumulateData() {
    assertThat(aggregator.accumulateDouble(2.0))
        .isEqualTo(
            HistogramAccumulation.create(1, 2.0, Collections.emptyList(), ImmutableList.of(1L)));
    assertThat(aggregator.accumulateLong(10))
        .isEqualTo(
            HistogramAccumulation.create(1, 10.0, Collections.emptyList(), ImmutableList.of(1L)));
  }

  @Test
  void testMultithreadedUpdates() throws Exception {
    final AggregatorHandle<HistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    final Histogram summarizer = new Histogram();
    int numberOfThreads = 10;
    final long[] updates = new long[] {1, 2, 3, 5, 7, 11, 13, 17, 19, 23};
    final int numberOfUpdates = 1000;
    final CountDownLatch startingGun = new CountDownLatch(numberOfThreads);
    List<Thread> workers = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      final int index = i;
      Thread t =
          new Thread(
              () -> {
                long update = updates[index];
                try {
                  startingGun.await();
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
                for (int j = 0; j < numberOfUpdates; j++) {
                  aggregatorHandle.recordLong(update);
                  if (ThreadLocalRandom.current().nextInt(10) == 0) {
                    summarizer.process(aggregatorHandle.accumulateThenReset());
                  }
                }
              });
      workers.add(t);
      t.start();
    }
    for (int i = 0; i <= numberOfThreads; i++) {
      startingGun.countDown();
    }

    for (Thread worker : workers) {
      worker.join();
    }
    // make sure everything gets merged when all the aggregation is done.
    summarizer.process(aggregatorHandle.accumulateThenReset());

    assertThat(summarizer.accumulation)
        .isEqualTo(
            HistogramAccumulation.create(
                numberOfThreads * numberOfUpdates,
                101000,
                BUCKET_BOUNDARIES,
                ImmutableList.of(5000L, 5000L, 0L, 0L)));
  }

  private static final class Histogram {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    @Nullable
    private HistogramAccumulation accumulation;

    void process(@Nullable HistogramAccumulation other) {
      if (other == null) {
        return;
      }
      lock.writeLock().lock();
      try {
        if (accumulation == null) {
          accumulation = other;
          return;
        }
        accumulation = aggregator.merge(accumulation, other);
      } finally {
        lock.writeLock().unlock();
      }
    }
  }
}
