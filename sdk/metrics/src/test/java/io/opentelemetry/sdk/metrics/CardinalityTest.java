/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CardinalityTest {

  /**
   * Records to sync and async instruments, with distinct attributes each time. Validates that stale
   * metrics are dropped for delta and cumulative readers, sync and async instruments. Stale metrics
   * are those with attributes that did not receive recordings in the most recent collection.
   */
  @Test
  void staleMetricsDropped() {
    InMemoryMetricReader deltaReader = InMemoryMetricReader.createDelta();
    InMemoryMetricReader cumulativeReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(deltaReader)
            .registerMetricReader(cumulativeReader)
            .setMinimumCollectionInterval(Duration.ofSeconds(0))
            .build();
    Meter meter = sdkMeterProvider.get(CardinalityTest.class.getName());

    AtomicLong count = new AtomicLong();
    meter
        .counterBuilder("async-counter")
        .buildWithCallback(
            measurement ->
                measurement.observe(
                    1, Attributes.builder().put("key", "num_" + count.incrementAndGet()).build()));
    LongCounter syncCounter = meter.counterBuilder("sync-counter").build();

    for (int i = 1; i <= 5; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + count.incrementAndGet()).build());

      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .hasSize(2)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSum()
                      .isDelta()
                      .points()
                      .hasSize(1),
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSum()
                      .isDelta()
                      .points()
                      .hasSize(1));

      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .hasSize(2)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSum()
                      .isCumulative()
                      .points()
                      .hasSize(1),
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSum()
                      .isCumulative()
                      .points()
                      .hasSize(1));
    }
  }
}
