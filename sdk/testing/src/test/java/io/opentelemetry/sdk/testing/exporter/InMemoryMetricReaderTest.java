/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryMetricReaderTest {

  private SdkMeterProvider provider;
  private InMemoryMetricReader cumulativeReader;
  private InMemoryMetricReader deltaReader;

  @BeforeEach
  void setup() {
    cumulativeReader = InMemoryMetricReader.create();
    deltaReader = InMemoryMetricReader.createDelta();
    provider =
        SdkMeterProvider.builder()
            .registerMetricReader(cumulativeReader)
            .registerMetricReader(deltaReader)
            .build();
  }

  private void generateFakeMetric(int index) {
    provider.get("test").counterBuilder("test" + index).build().add(1);
  }

  @Test
  void collectAllMetrics() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);

    assertThat(cumulativeReader.collectAllMetrics()).hasSize(3);
    assertThat(deltaReader.collectAllMetrics()).hasSize(3);
  }

  @Test
  void collectAllMetrics_BeforeApply() {
    assertThat(InMemoryMetricReader.create().collectAllMetrics()).isEmpty();
    assertThat(InMemoryMetricReader.createDelta().collectAllMetrics()).isEmpty();
  }

  @Test
  void reset() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);

    assertThat(cumulativeReader.collectAllMetrics()).hasSize(3);
    assertThat(deltaReader.collectAllMetrics()).hasSize(3);

    // Add more data, should join.
    generateFakeMetric(1);
    assertThat(cumulativeReader.collectAllMetrics()).hasSize(3);
    assertThat(deltaReader.collectAllMetrics()).hasSize(1);
  }

  @Test
  void flush() {
    generateFakeMetric(1);
    generateFakeMetric(2);
    generateFakeMetric(3);
    // TODO: Better assertions for CompletableResultCode.
    assertThat(cumulativeReader.forceFlush()).isNotNull();
    assertThat(deltaReader.forceFlush()).isNotNull();

    assertThat(cumulativeReader.collectAllMetrics()).hasSize(3);
    assertThat(deltaReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void shutdown_Cumulative() {
    generateFakeMetric(1);
    assertThat(cumulativeReader.collectAllMetrics()).hasSize(1);
    assertThat(deltaReader.collectAllMetrics()).hasSize(1);

    assertThat(cumulativeReader.shutdown()).isNotNull();
    assertThat(deltaReader.shutdown()).isNotNull();

    // Post shutdown, collectAllMetrics should not be called.
    assertThat(cumulativeReader.collectAllMetrics()).hasSize(0);
    assertThat(deltaReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void stringRepresentation() {
    assertThat(deltaReader.toString())
        .isEqualTo("InMemoryMetricReader{aggregationTemporality=DELTA}");
  }
}
