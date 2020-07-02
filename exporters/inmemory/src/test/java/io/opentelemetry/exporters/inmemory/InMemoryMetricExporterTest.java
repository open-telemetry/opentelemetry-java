package io.opentelemetry.exporters.inmemory;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.export.MetricExporter.ResultCode;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

/** Unit tests for {@link InMemoryMetricExporter}. */
@RunWith(JUnit4.class)
public class InMemoryMetricExporterTest {

  private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();

  @Test
  public void test_getFinishedMetricItems() {
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
  public void test_reset() {
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
  public void test_shutdown() {
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
  public void testShutdown_export() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.SUCCESS);
    exporter.shutdown();
    assertThat(exporter.export(metrics)).isEqualTo(ResultCode.FAILURE);
  }

  @Test
  public void test_flush() {
    assertThat(exporter.flush()).isEqualTo(ResultCode.SUCCESS);
  }


  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.create(
        Descriptor.create("name", "description", "1", Type.MONOTONIC_LONG, Labels.empty()),
        Resource.getEmpty(),
        InstrumentationLibraryInfo.getEmpty(),
        Collections.singletonList(LongPoint.create(startNs, endNs, Labels.of("k", "v"), 5)));
  }

}
