/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.internal.state.DefaultSynchronousMetricStorage;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressLogger(
    loggerName = "io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage")
@SuppressLogger(DefaultSynchronousMetricStorage.class)
class CardinalityTest {

  /** Traces {@code MetricStorageUtils#MAX_CARDINALITY}. */
  private static final int MAX_CARDINALITY = 2000;

  private InMemoryMetricReader deltaReader;
  private InMemoryMetricReader cumulativeReader;
  private Meter meter;

  @BeforeEach
  void setup() {
    deltaReader = InMemoryMetricReader.createDelta();
    cumulativeReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(deltaReader)
            .registerMetricReader(cumulativeReader)
            .build();
    meter = sdkMeterProvider.get(CardinalityTest.class.getName());
  }

  /**
   * Records to sync instruments, with distinct attributes each time. Validates that stale metrics
   * are dropped for delta and cumulative readers. Stale metrics are those with attributes that did
   * not receive recordings in the most recent collection.
   *
   * <p>Effectively, we make sure we cap-out at attribute size = 2000 (constant in
   * MetricStorageutils).
   */
  @Test
  void staleMetricsDropped_synchronousInstrument() {
    LongCounter syncCounter = meter.counterBuilder("sync-counter").build();
    // Note: This constant comes from MetricStorageUtils, but it's package-private.
    for (int i = 1; i <= 2000; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + i).build());

      // DELTA reader only has latest
      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSumSatisfying(sum -> sum.isDelta().hasPointsSatisfying(point -> {})));

      // Make sure we preserve previous cumulatives
      int currentSize = i;
      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSumSatisfying(
                          sum ->
                              sum.isCumulative()
                                  .satisfies(
                                      (Consumer<SumData<LongPointData>>)
                                          sumPointData ->
                                              assertThat(sumPointData.getPoints().size())
                                                  .isEqualTo(currentSize))));
    }
    // Now punch the limit and ONLY metrics we just recorded stay, due to simplistic GC.
    for (int i = 2001; i <= 2010; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + i).build());
    }
    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection - post limit @ 10")
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(10))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection - post limit @ 10")
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(2000))));
  }

  /**
   * Records to async instruments, with distinct attributes each time. Validates that stale metrics
   * are dropped for delta and cumulative readers. Stale metrics are those with attributes that did
   * not receive recordings in the most recent collection.
   */
  @Test
  void staleMetricsDropped_asynchronousInstrument() {
    AtomicLong count = new AtomicLong();

    meter
        .counterBuilder("async-counter")
        .buildWithCallback(
            measurement ->
                measurement.record(
                    1, Attributes.builder().put("key", "num_" + count.incrementAndGet()).build()));

    for (int i = 1; i <= 5; i++) {
      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSumSatisfying(sum -> sum.isDelta().hasPointsSatisfying(point -> {})));

      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSumSatisfying(
                          sum -> sum.isCumulative().hasPointsSatisfying(point -> {})));
    }
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_CARDINALITY} is enforced for each instrument.
   */
  @Test
  void cardinalityLimits_synchronousInstrument() {
    LongCounter syncCounter1 = meter.counterBuilder("sync-counter1").build();
    LongCounter syncCounter2 = meter.counterBuilder("sync-counter2").build();
    for (int i = 0; i < MAX_CARDINALITY + 1; i++) {
      syncCounter1.add(1, Attributes.builder().put("key", "value" + i).build());
      syncCounter2.add(1, Attributes.builder().put("key", "value" + i).build());
    }

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))));
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_CARDINALITY} is enforced for each instrument.
   */
  @Test
  void cardinalityLimits_asynchronousInstrument() {
    Consumer<ObservableLongMeasurement> callback =
        measurement -> {
          for (int i = 0; i < MAX_CARDINALITY + 1; i++) {
            measurement.record(1, Attributes.builder().put("key", "value" + i).build());
          }
        };
    meter.counterBuilder("async-counter1").buildWithCallback(callback);
    meter.counterBuilder("async-counter2").buildWithCallback(callback);

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(MAX_CARDINALITY))));
  }
}
