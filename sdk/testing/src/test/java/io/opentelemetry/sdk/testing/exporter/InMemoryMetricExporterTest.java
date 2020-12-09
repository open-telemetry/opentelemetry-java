/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryMetricExporter}. */
class InMemoryMetricExporterTest {

  private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.create(
        Resource.getEmpty(),
        InstrumentationLibraryInfo.getEmpty(),
        "name",
        "description",
        "1",
        MetricData.Type.LONG_SUM,
        Collections.singletonList(LongPoint.create(startNs, endNs, Labels.of("k", "v"), 5)));
  }

  @Test
  void test_getFinishedMetricItems() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(3);
  }

  @Test
  void test_reset() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(3);
    exporter.reset();
    metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(0);
  }

  @Test
  void test_shutdown() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    exporter.shutdown();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(0);
  }

  @Test
  void testShutdown_export() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    exporter.shutdown();
    assertThat(exporter.export(metrics).isSuccess()).isFalse();
  }

  @Test
  void test_flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
  }
}
