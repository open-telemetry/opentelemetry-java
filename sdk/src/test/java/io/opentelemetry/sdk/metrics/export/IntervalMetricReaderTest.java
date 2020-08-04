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

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilderTest.ConfigTester;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link IntervalMetricReader}. */
class IntervalMetricReaderTest {
  private static final MetricData.Descriptor METRIC_DESCRIPTOR =
      MetricData.Descriptor.create(
          "my metric",
          "my metric description",
          "us",
          Descriptor.Type.MONOTONIC_LONG,
          Labels.empty());

  private static final List<Point> LONG_POINT_LIST =
      Collections.singletonList(LongPoint.create(1000, 3000, Labels.empty(), 1234567));

  private static final MetricData METRIC_DATA =
      MetricData.create(
          METRIC_DESCRIPTOR,
          Resource.getEmpty(),
          InstrumentationLibraryInfo.create("IntervalMetricReaderTest", null),
          LONG_POINT_LIST);

  @Mock private MetricProducer metricProducer;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);
    when(metricProducer.collectAllMetrics()).thenReturn(Collections.singletonList(METRIC_DATA));
  }

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    options.put("otel.imr.export.interval", "12");
    IntervalMetricReader.Builder config = IntervalMetricReader.builder();
    IntervalMetricReader.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigTester.getNamingDot());
    Mockito.verify(spy).setExportIntervalMillis(12);
  }

  @Test
  void intervalExport() {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.builder()
            .setExportIntervalMillis(100)
            .setMetricExporter(waitingMetricExporter)
            .setMetricProducers(Collections.singletonList(metricProducer))
            .build();

    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(1))
          .containsExactly(Collections.singletonList(METRIC_DATA));

      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(METRIC_DATA), Collections.singletonList(METRIC_DATA));
    } finally {
      intervalMetricReader.shutdown();
    }
  }

  @Test
  @Timeout(2)
  public void intervalExport_exporterThrowsException() {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter(/* shouldThrow=*/ true);
    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.builder()
            .setExportIntervalMillis(100)
            .setMetricExporter(waitingMetricExporter)
            .setMetricProducers(Collections.singletonList(metricProducer))
            .build();

    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(1))
          .containsExactly(Collections.singletonList(METRIC_DATA));

      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(METRIC_DATA), Collections.singletonList(METRIC_DATA));
    } finally {
      intervalMetricReader.shutdown();
    }
  }

  @Test
  void oneLastExportAfterShutdown() {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.builder()
            .setExportIntervalMillis(100_000)
            .setMetricExporter(waitingMetricExporter)
            .setMetricProducers(Collections.singletonList(metricProducer))
            .build();

    // Assume that this will be called in less than 100 seconds.
    intervalMetricReader.shutdown();

    // This export was called during shutdown.
    assertThat(waitingMetricExporter.waitForNumberOfExports(1))
        .containsExactly(Collections.singletonList(METRIC_DATA));

    assertThat(waitingMetricExporter.hasShutdown.get()).isTrue();
  }

  private static class WaitingMetricExporter implements MetricExporter {

    private final Object monitor = new Object();
    private final AtomicBoolean hasShutdown = new AtomicBoolean(false);
    private final boolean shouldThrow;

    @GuardedBy("monitor")
    private List<List<MetricData>> exportedMetrics = new ArrayList<>();

    private WaitingMetricExporter() {
      this(false);
    }

    private WaitingMetricExporter(boolean shouldThrow) {
      this.shouldThrow = shouldThrow;
    }

    @Override
    public ResultCode export(Collection<MetricData> metricList) {
      synchronized (monitor) {
        this.exportedMetrics.add(new ArrayList<>(metricList));
        monitor.notifyAll();
      }
      if (shouldThrow) {
        throw new RuntimeException("Export Failed!");
      }
      return ResultCode.SUCCESS;
    }

    @Override
    public ResultCode flush() {
      return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {
      hasShutdown.set(true);
    }

    /**
     * Waits until export is called for numberOfExports times. Returns the list of exported lists of
     * metrics.
     */
    @Nullable
    List<List<MetricData>> waitForNumberOfExports(int numberOfExports) {
      List<List<MetricData>> result;
      synchronized (monitor) {
        while (exportedMetrics.size() < numberOfExports) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Preserve the interruption status as per guidance.
            Thread.currentThread().interrupt();
            return null;
          }
        }
        result = exportedMetrics;
        exportedMetrics = new ArrayList<>();
      }
      return result;
    }
  }
}
