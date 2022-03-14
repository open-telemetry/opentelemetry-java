/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PeriodicMetricReaderTest {
  private static final List<LongPointData> LONG_POINT_LIST =
      Collections.singletonList(
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 1234567));

  private static final MetricData METRIC_DATA =
      ImmutableMetricData.createLongSum(
          Resource.empty(),
          InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
          "my metric",
          "my metric description",
          "us",
          ImmutableSumData.create(
              /* isMonotonic= */ true, AggregationTemporality.CUMULATIVE, LONG_POINT_LIST));

  @Mock private MetricProducer metricProducer;
  @Mock private MetricExporter metricExporter;

  @BeforeEach
  void setup() {
    when(metricProducer.collectAllMetrics()).thenReturn(Collections.singletonList(METRIC_DATA));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void startOnlyOnce() {
    ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

    ScheduledFuture mock = mock(ScheduledFuture.class);
    when(scheduler.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(mock);

    MetricReaderFactory factory =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofMillis(1))
            .setExecutor(scheduler)
            .newMetricReaderFactory();

    // Starts the interval reader.
    factory.apply(metricProducer);

    verify(scheduler, times(1)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
  }

  @Test
  void periodicExport() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    MetricReaderFactory factory =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .newMetricReaderFactory();

    MetricReader reader = factory.apply(metricProducer);
    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(1))
          .containsExactly(Collections.singletonList(METRIC_DATA));

      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(METRIC_DATA), Collections.singletonList(METRIC_DATA));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  void flush() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    MetricReaderFactory factory =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofNanos(Long.MAX_VALUE))
            .newMetricReaderFactory();

    MetricReader reader = factory.apply(metricProducer);
    assertThat(reader.flush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(1))
          .containsExactly(Collections.singletonList(METRIC_DATA));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  @Timeout(2)
  @SuppressLogger(PeriodicMetricReader.class)
  public void intervalExport_exporterThrowsException() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter(/* shouldThrow=*/ true);
    MetricReaderFactory factory =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .newMetricReaderFactory();
    MetricReader reader = factory.apply(metricProducer);
    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(METRIC_DATA), Collections.singletonList(METRIC_DATA));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  void oneLastExportAfterShutdown() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    MetricReaderFactory factory =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofSeconds(100))
            .newMetricReaderFactory();
    MetricReader reader = factory.apply(metricProducer);
    // Assume that this will be called in less than 100 seconds.
    reader.shutdown();

    // This export was called during shutdown.
    assertThat(waitingMetricExporter.waitForNumberOfExports(1))
        .containsExactly(Collections.singletonList(METRIC_DATA));

    assertThat(waitingMetricExporter.hasShutdown.get()).isTrue();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload") // Testing the overload
  void invalidConfig() {
    assertThatThrownBy(() -> PeriodicMetricReader.builder(metricExporter).setInterval(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(
            () ->
                PeriodicMetricReader.builder(metricExporter).setInterval(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("interval must be positive");
    assertThatThrownBy(() -> PeriodicMetricReader.builder(metricExporter).setInterval(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("interval");
    assertThatThrownBy(() -> PeriodicMetricReader.builder(metricExporter).setExecutor(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("executor");
  }

  private static class WaitingMetricExporter implements MetricExporter {

    private final AtomicBoolean hasShutdown = new AtomicBoolean(false);
    private final boolean shouldThrow;
    private final BlockingQueue<List<MetricData>> queue = new LinkedBlockingQueue<>();
    private final List<Long> exportTimes = Collections.synchronizedList(new ArrayList<>());

    private WaitingMetricExporter() {
      this(false);
    }

    private WaitingMetricExporter(boolean shouldThrow) {
      this.shouldThrow = shouldThrow;
    }

    @Override
    public AggregationTemporality getPreferredTemporality() {
      return null;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metricList) {
      exportTimes.add(System.currentTimeMillis());
      queue.offer(new ArrayList<>(metricList));

      if (shouldThrow) {
        throw new RuntimeException("Export Failed!");
      }
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      hasShutdown.set(true);
      return CompletableResultCode.ofSuccess();
    }

    /**
     * Waits until export is called for numberOfExports times. Returns the list of exported lists of
     * metrics.
     */
    @Nullable
    List<List<MetricData>> waitForNumberOfExports(int numberOfExports) throws Exception {
      List<List<MetricData>> result = new ArrayList<>();
      while (result.size() < numberOfExports) {
        List<MetricData> export = queue.poll(5, TimeUnit.SECONDS);
        assertThat(export).isNotNull();
        result.add(export);
      }
      return result;
    }
  }
}
