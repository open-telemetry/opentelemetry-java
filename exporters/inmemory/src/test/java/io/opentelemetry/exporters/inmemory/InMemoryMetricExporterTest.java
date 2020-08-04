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

package io.opentelemetry.exporters.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.export.MetricExporter.ResultCode;
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
        Descriptor.create(
            "name", "description", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
        Resource.getEmpty(),
        InstrumentationLibraryInfo.getEmpty(),
        Collections.singletonList(LongPoint.create(startNs, endNs, Labels.of("k", "v"), 5)));
  }

  @Test
  void test_getFinishedMetricItems() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.SUCCESS);
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

    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.SUCCESS);
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

    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.SUCCESS);
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

    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.SUCCESS);
    exporter.shutdown();
    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.FAILURE);
  }

  @Test
  void test_flush() {
    assertThat(exporter.flush()).isEqualTo(ResultCode.SUCCESS);
  }
}
