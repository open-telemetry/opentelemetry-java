/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
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
    return ImmutableMetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "name",
        "description",
        "1",
        ImmutableSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableLongPointData.create(
                    startNs, endNs, Attributes.builder().put("k", "v").build(), 5))));
  }

  @Test
  void getAggregationTemporality() {
    assertThat(InMemoryMetricExporter.create().getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            InMemoryMetricExporter.create(AggregationTemporality.DELTA)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void getFinishedMetricItems() {
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
  void reset() {
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
  void shutdown() {
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
  void shutdown_ThenExport() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    exporter.shutdown();
    assertThat(exporter.export(metrics).isSuccess()).isFalse();
  }

  @Test
  void flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
  }
}
