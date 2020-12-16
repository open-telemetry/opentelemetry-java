/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.DoubleSummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

class DoubleMinMaxSumCountTest {

  @Test
  void testRecordings() {
    Aggregator aggregator = DoubleMinMaxSumCount.getFactory().getAggregator();

    assertThat(aggregator.toPoint(0, 100, Labels.empty())).isNull();

    aggregator.recordDouble(100);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            DoubleSummaryPoint.create(
                0, 100, Labels.empty(), 1, 100, createPercentiles(100d, 100d)));

    aggregator.recordDouble(50);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            DoubleSummaryPoint.create(
                0, 100, Labels.empty(), 2, 150, createPercentiles(50d, 100d)));

    aggregator.recordDouble(-75);
    assertThat(aggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            DoubleSummaryPoint.create(
                0, 100, Labels.empty(), 3, 75, createPercentiles(-75d, 100d)));
  }

  @Test
  void testMergeAndReset() {
    Aggregator aggregator = DoubleMinMaxSumCount.getFactory().getAggregator();

    aggregator.recordDouble(100);
    Aggregator mergedToAggregator = DoubleMinMaxSumCount.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedToAggregator);

    assertThat(mergedToAggregator.toPoint(0, 100, Labels.empty()))
        .isEqualTo(
            DoubleSummaryPoint.create(
                0, 100, Labels.empty(), 1, 100, createPercentiles(100d, 100d)));

    assertThat(aggregator.toPoint(0, 100, Labels.empty())).isNull();
  }

  @Test
  void testMultithreadedUpdates() throws Exception {
    final Aggregator aggregator = DoubleMinMaxSumCount.getFactory().getAggregator();
    final Aggregator summarizer = DoubleMinMaxSumCount.getFactory().getAggregator();
    int numberOfThreads = 10;
    final double[] updates = new double[] {1.1, 2.1, 3.1, 5.1, 7.1, 11.1, 13.1, 17.1, 19.1, 23.1};
    final int numberOfUpdates = 1000;
    final CountDownLatch startingGun = new CountDownLatch(numberOfThreads);
    List<Thread> workers = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      final int index = i;
      Thread t =
          new Thread(
              () -> {
                double update = updates[index];
                try {
                  startingGun.await();
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
                for (int j = 0; j < numberOfUpdates; j++) {
                  aggregator.recordDouble(update);
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

    DoubleSummaryPoint actual = (DoubleSummaryPoint) summarizer.toPoint(0, 100, Labels.empty());
    assertThat(actual).isNotNull();
    assertThat(actual.getStartEpochNanos()).isEqualTo(0);
    assertThat(actual.getEpochNanos()).isEqualTo(100);
    assertThat(actual.getLabels()).isEqualTo(Labels.empty());
    assertThat(actual.getCount()).isEqualTo(numberOfThreads * numberOfUpdates);
    assertThat(actual.getSum()).isCloseTo(102000d, offset(0.001));
    List<ValueAtPercentile> percentileValues = actual.getPercentileValues();
    assertThat(percentileValues).isEqualTo(createPercentiles(1.1d, 23.1d));
  }

  private static List<ValueAtPercentile> createPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0.0, min), ValueAtPercentile.create(100.0, max));
  }
}
