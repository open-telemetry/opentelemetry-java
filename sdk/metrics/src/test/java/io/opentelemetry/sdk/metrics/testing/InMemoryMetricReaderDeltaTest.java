/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryMetricExporter}. */
class InMemoryMetricReaderDeltaTest {

  private SdkMeterProvider provider;
  private InMemoryMetricReader reader;

  @BeforeEach
  void setup() {
    reader = InMemoryMetricReader.createDelta();
    provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
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
  void test_reset() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);

    assertThat(reader.collectAllMetrics()).hasSize(3);
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void test_flush() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);
    // TODO: Better assertions for CompletableResultCode.
    assertThat(reader.flush()).isNotNull();
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void test_shutdown() {
    generateFakeMetric(1);
    assertThat(reader.collectAllMetrics()).hasSize(1);
    assertThat(reader.shutdown()).isNotNull();
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }
}
