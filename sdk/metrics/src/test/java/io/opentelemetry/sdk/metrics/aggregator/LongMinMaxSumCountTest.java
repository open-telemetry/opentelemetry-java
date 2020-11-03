/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

class LongMinMaxSumCountTest {

  @Test
  void testRecordings() {
    Aggregator aggregator = LongMinMaxSumCount.getFactory().getAggregator();

    assertThat(aggregator.toPoint(0, 100, Labels.empty())).isNull();

    aggregator.recordLong(100);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            SummaryPoint.create(
                0, 100, Labels.empty(), 1, 100, createPercentileValues(100L, 100L)));

    aggregator.recordLong(50);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            SummaryPoint.create(0, 100, Labels.empty(), 2, 150, createPercentileValues(50L, 100L)));

    aggregator.recordLong(-75);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            SummaryPoint.create(0, 100, Labels.empty(), 3, 75, createPercentileValues(-75L, 100L)));
  }

  @Test
  void testMergeAndReset() {
    Aggregator aggregator = LongMinMaxSumCount.getFactory().getAggregator();

    aggregator.recordLong(100);
    Aggregator mergedToAggregator = LongMinMaxSumCount.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedToAggregator);

    assertThat(mergedToAggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            SummaryPoint.create(
                0, 100, Labels.empty(), 1, 100, createPercentileValues(100L, 100L)));

    assertThat(aggregator.toPoint(0, 100, Labels.empty())).isNull();
  }

  @Test
  void testMultithreadedUpdates() throws Exception {
    final Aggregator aggregator = LongMinMaxSumCount.getFactory().getAggregator();
    final Aggregator summarizer = LongMinMaxSumCount.getFactory().getAggregator();
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
                  aggregator.recordLong(update);
                  if (ThreadLocalRandom.current().nextInt(10) == 0) {
                    aggregator.mergeToAndReset(summarizer);
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
    aggregator.mergeToAndReset(summarizer);

    assertThat(summarizer.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            SummaryPoint.create(
                0,
                100,
                Labels.empty(),
                numberOfThreads * numberOfUpdates,
                101000,
                createPercentileValues(1L, 23L)));
  }

  private static List<ValueAtPercentile> createPercentileValues(long min, long max) {
    return Arrays.asList(ValueAtPercentile.create(0.0, min), ValueAtPercentile.create(100.0, max));
  }
}
