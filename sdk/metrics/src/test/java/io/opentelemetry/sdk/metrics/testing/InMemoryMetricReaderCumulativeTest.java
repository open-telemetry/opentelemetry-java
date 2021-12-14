/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Testing class which will be removed
class InMemoryMetricReaderCumulativeTest {

  private SdkMeterProvider provider;
  private InMemoryMetricReader reader;

  @BeforeEach
  void setup() {
    reader = InMemoryMetricReader.create();
    provider =
        SdkMeterProvider.builder()
            .setMinimumCollectionInterval(Duration.ofSeconds(0))
            .registerMetricReader(reader)
            .build();
  }

  private void generateFakeMetric(int index) {
    provider.get("test").counterBuilder("test" + index).build().add(1);
  }

  @Test
  void test_collectAllMetrics() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);

    assertThat(reader.collectAllMetrics()).hasSize(3);
  }

  @Test
  void test_reset_preserves_cumulatives() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);

    assertThat(reader.collectAllMetrics()).hasSize(3);

    // Add more data, should join.
    generateFakeMetric(1);
    assertThat(reader.collectAllMetrics()).hasSize(3);
  }

  @Test
  void test_flush() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);
    // TODO: Better assertions for CompletableResultCode.
    assertThat(reader.flush()).isNotNull();
    assertThat(reader.collectAllMetrics()).hasSize(3);
  }

  @Test
  void test_shutdown() {
    generateFakeMetric(1);
    assertThat(reader.collectAllMetrics()).hasSize(1);
    assertThat(reader.shutdown()).isNotNull();
    // Post shutdown, collectAllMetrics should not be called.
  }
}
