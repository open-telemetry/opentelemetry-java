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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
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

  @Mock private CollectionRegistration collectionRegistration;
  @Mock private MetricExporter metricExporter;

  @BeforeEach
  void setup() {
    when(collectionRegistration.collectAllMetrics())
        .thenReturn(Collections.singletonList(METRIC_DATA));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void startOnlyOnce() {

    ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

    ScheduledFuture mock = mock(ScheduledFuture.class);
    when(scheduler.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(mock);

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofMillis(1))
            .setExecutor(scheduler)
            .build();

    reader.register(collectionRegistration);

    verify(scheduler, times(1)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
  }

  @Test
  void periodicExport() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .build();

    reader.register(collectionRegistration);
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
  void periodicExport_NoMetricsSkipsExport() {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .build();
    when(collectionRegistration.collectAllMetrics()).thenReturn(Collections.emptyList());
    reader.register(collectionRegistration);

    try {
      assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      verify(collectionRegistration).collectAllMetrics();
      assertThat(waitingMetricExporter.exportTimes.size()).isEqualTo(0);
    } finally {
      reader.shutdown();
    }
  }

  @Test
  void flush() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofNanos(Long.MAX_VALUE))
            .build();

    reader.register(collectionRegistration);
    assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(1))
          .containsExactly(Collections.singletonList(METRIC_DATA));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  @SuppressLogger(PeriodicMetricReader.class)
  void forceflush_callsFlush() {
    MetricExporter metricExporter = mock(MetricExporter.class);
    when(metricExporter.export(any()))
        .thenReturn(CompletableResultCode.ofSuccess())
        .thenReturn(CompletableResultCode.ofSuccess())
        .thenThrow(new RuntimeException("Export Failed!"));
    when(metricExporter.flush())
        .thenReturn(CompletableResultCode.ofSuccess())
        .thenReturn(CompletableResultCode.ofFailure())
        .thenReturn(CompletableResultCode.ofSuccess());
    when(metricExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofNanos(Long.MAX_VALUE))
            .build();

    try {
      reader.register(collectionRegistration);
      assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
      assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    } finally {
      reader.shutdown();
    }
    verify(metricExporter, times(3)).flush();
  }

  @Test
  @Timeout(2)
  @SuppressLogger(PeriodicMetricReader.class)
  public void intervalExport_exporterThrowsException() throws Exception {
    WaitingMetricExporter waitingMetricExporter =
        new WaitingMetricExporter(/* shouldThrow= */ true);
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .build();

    reader.register(collectionRegistration);
    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(METRIC_DATA), Collections.singletonList(METRIC_DATA));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  void shutdown_ExportsOneLastTime() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
            .build();
    reader.register(collectionRegistration);
    reader.shutdown();

    // This export was called during shutdown.
    assertThat(waitingMetricExporter.waitForNumberOfExports(1))
        .containsExactly(Collections.singletonList(METRIC_DATA));

    assertThat(waitingMetricExporter.hasShutdown.get()).isTrue();
  }

  @Test
  void close_CallsShutdown() throws IOException {
    PeriodicMetricReader reader =
        spy(
            PeriodicMetricReader.builder(new WaitingMetricExporter())
                .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
                .build());
    reader.register(collectionRegistration);
    reader.close();

    verify(reader, times(1)).shutdown();
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

  @Test
  void stringRepresentation() {
    when(metricExporter.toString()).thenReturn("MockMetricExporter{}");
    assertThat(
            PeriodicMetricReader.builder(metricExporter)
                .setInterval(Duration.ofSeconds(1))
                .build()
                .toString())
        .isEqualTo(
            "PeriodicMetricReader{"
                + "exporter=MockMetricExporter{}, "
                + "intervalNanos=1000000000"
                + "}");
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
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
      return AggregationTemporality.CUMULATIVE;
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
