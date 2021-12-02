/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardinalityTest {

  /** Traces {@code MetricStorageUtils#MAX_ACCUMULATIONS}. */
  private static final int MAX_ACCUMULATIONS = 2000;

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
            .setMinimumCollectionInterval(Duration.ofSeconds(0))
            .build();
    meter = sdkMeterProvider.get(CardinalityTest.class.getName());
  }

  /**
   * Records to sync instruments, with distinct attributes each time. Validates that stale metrics
   * are dropped for delta and cumulative readers. Stale metrics are those with attributes that did
   * not receive recordings in the most recent collection.
   */
  @Test
  void staleMetricsDropped_synchronousInstrument() {
    LongCounter syncCounter = meter.counterBuilder("sync-counter").build();
    for (int i = 1; i <= 5; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + i).build());

      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .hasSize(1)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSum()
                      .isDelta()
                      .points()
                      .hasSize(1));

      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .hasSize(1)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSum()
                      .isCumulative()
                      .points()
                      .hasSize(1));
    }
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
          .hasSize(1)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSum()
                      .isDelta()
                      .points()
                      .hasSize(1));

      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .hasSize(1)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSum()
                      .isCumulative()
                      .points()
                      .hasSize(1));
    }
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_ACCUMULATIONS} is enforced for each instrument.
   */
  @Test
  void cardinalityLimits_synchronousInstrument() {
    LongCounter syncCounter1 = meter.counterBuilder("sync-counter1").build();
    LongCounter syncCounter2 = meter.counterBuilder("sync-counter2").build();
    for (int i = 0; i < MAX_ACCUMULATIONS + 1; i++) {
      syncCounter1.add(1, Attributes.builder().put("key", "value" + i).build());
      syncCounter2.add(1, Attributes.builder().put("key", "value" + i).build());
    }

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .hasSize(2)
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .hasSize(2)
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS));
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_ACCUMULATIONS} is enforced for each instrument.
   */
  @Test
  void cardinalityLimits_asynchronousInstrument() {
    Consumer<ObservableLongMeasurement> callback =
        measurement -> {
          for (int i = 0; i < MAX_ACCUMULATIONS + 1; i++) {
            measurement.record(1, Attributes.builder().put("key", "value" + i).build());
          }
        };
    meter.counterBuilder("async-counter1").buildWithCallback(callback);
    meter.counterBuilder("async-counter2").buildWithCallback(callback);

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .hasSize(2)
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .hasSize(2)
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .hasSize(MAX_ACCUMULATIONS));
  }
}
