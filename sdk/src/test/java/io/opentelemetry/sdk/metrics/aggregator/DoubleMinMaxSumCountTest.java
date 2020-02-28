/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;

public class DoubleMinMaxSumCountTest {

  @Test
  public void testRecordings() {
    Aggregator aggregator = DoubleMinMaxSumCount.getFactory().getAggregator();

    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap())).isNull();

    aggregator.recordDouble(100);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            SummaryPoint.create(
                0,
                100,
                Collections.<String, String>emptyMap(),
                1,
                100,
                createPercentiles(100d, 100d)));

    aggregator.recordDouble(50);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            SummaryPoint.create(
                0,
                100,
                Collections.<String, String>emptyMap(),
                2,
                150,
                createPercentiles(50d, 100d)));

    aggregator.recordDouble(-75);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            SummaryPoint.create(
                0,
                100,
                Collections.<String, String>emptyMap(),
                3,
                75,
                createPercentiles(-75d, 100d)));
  }

  @Test
  public void testMergeAndReset() {
    Aggregator aggregator = DoubleMinMaxSumCount.getFactory().getAggregator();

    aggregator.recordDouble(100);
    Aggregator mergedToAggregator = DoubleMinMaxSumCount.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedToAggregator);

    assertThat(mergedToAggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            SummaryPoint.create(
                0,
                100,
                Collections.<String, String>emptyMap(),
                1,
                100,
                createPercentiles(100d, 100d)));

    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap())).isNull();
  }

  @Test
  public void testMultithreadedUpdates() throws Exception {
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
              new Runnable() {
                @Override
                public void run() {
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

    SummaryPoint actual =
        (SummaryPoint) summarizer.toPoint(0, 100, Collections.<String, String>emptyMap());
    assertThat(actual.getStartEpochNanos()).isEqualTo(0);
    assertThat(actual.getEpochNanos()).isEqualTo(100);
    assertThat(actual.getLabels()).isEqualTo(Collections.emptyMap());
    assertThat(actual.getCount()).isEqualTo(numberOfThreads * numberOfUpdates);
    assertThat(actual.getSum()).isWithin(0.001).of(102000d);
    List<ValueAtPercentile> percentileValues = actual.getPercentileValues();
    assertThat(percentileValues).isEqualTo(createPercentiles(1.1d, 23.1d));
  }

  private static List<ValueAtPercentile> createPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0.0, min), ValueAtPercentile.create(100.0, max));
  }
}
