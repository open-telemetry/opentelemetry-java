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

import io.github.netmikey.logunit.api.LogCapturer;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PeriodicMetricReaderTest {
  private static final List<LongPointData> LONG_POINT_LIST =
      Arrays.asList(
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 1L),
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 2L),
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 3L),
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 4L),
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 5L),
          ImmutableLongPointData.create(1000, 3000, Attributes.empty(), 6L));

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

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(PeriodicMetricReader.class.getName());

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
  void build_WithIllegalMaxExportSize() {
    assertThatThrownBy(
            () -> PeriodicMetricReader.builder(metricExporter).setMaxExportBatchSize(0).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");

    assertThatThrownBy(
            () -> PeriodicMetricReader.builder(metricExporter).setMaxExportBatchSize(-1).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");
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
  void periodicExport_WithMaxExportBatchSize_PartiallyFilledBatch() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .setMaxExportBatchSize(4)
            .build();

    reader.register(collectionRegistration);
    MetricData expectedMetricDataBatch1 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
            "my metric",
            "my metric description",
            "us",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                LONG_POINT_LIST.subList(0, 4)));
    MetricData expectedMetricDataBatch2 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
            "my metric",
            "my metric description",
            "us",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                LONG_POINT_LIST.subList(4, 6)));
    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(2))
          .containsExactly(
              Collections.singletonList(expectedMetricDataBatch1),
              Collections.singletonList(expectedMetricDataBatch2));
    } finally {
      reader.shutdown();
    }
  }

  @Test
  void periodicExport_WithMaxExportBatchSize_CompletelyFilledBatch() throws Exception {
    WaitingMetricExporter waitingMetricExporter = new WaitingMetricExporter();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(waitingMetricExporter)
            .setInterval(Duration.ofMillis(100))
            .setMaxExportBatchSize(2)
            .build();

    reader.register(collectionRegistration);
    MetricData expectedMetricDataBatch1 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
            "my metric",
            "my metric description",
            "us",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                LONG_POINT_LIST.subList(0, 2)));
    MetricData expectedMetricDataBatch2 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
            "my metric",
            "my metric description",
            "us",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                LONG_POINT_LIST.subList(2, 4)));

    MetricData expectedMetricDataBatch3 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.create("PeriodicMetricReaderTest"),
            "my metric",
            "my metric description",
            "us",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                LONG_POINT_LIST.subList(4, 6)));
    try {
      assertThat(waitingMetricExporter.waitForNumberOfExports(3))
          .containsExactly(
              Collections.singletonList(expectedMetricDataBatch1),
              Collections.singletonList(expectedMetricDataBatch2),
              Collections.singletonList(expectedMetricDataBatch3));
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
  @Timeout(10)
  void shutdown_whileExportInFlight_waitsThenPerformsFinalExport() throws Exception {
    CompletableResultCode inflightExportResult = new CompletableResultCode();
    CountDownLatch exportStarted = new CountDownLatch(1);
    AtomicInteger exportCount = new AtomicInteger();
    AtomicBoolean shutdownCalledWhileExportPending = new AtomicBoolean();

    MetricExporter blockingExporter =
        new MetricExporter() {
          @Override
          public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
            return AggregationTemporality.CUMULATIVE;
          }

          @Override
          public CompletableResultCode export(Collection<MetricData> metrics) {
            if (exportCount.incrementAndGet() == 1) {
              exportStarted.countDown();
              return inflightExportResult;
            }
            return CompletableResultCode.ofSuccess();
          }

          @Override
          public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
          }

          @Override
          public CompletableResultCode shutdown() {
            if (!inflightExportResult.isDone()) {
              shutdownCalledWhileExportPending.set(true);
            }
            return CompletableResultCode.ofSuccess();
          }
        };

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(blockingExporter)
            .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
            .build();
    reader.register(collectionRegistration);

    // Trigger an export that blocks
    CompletableResultCode flushResult = reader.forceFlush();
    assertThat(exportStarted.await(5, TimeUnit.SECONDS)).isTrue();

    // Shutdown in background — should block waiting for in-flight export
    CountDownLatch shutdownDone = new CountDownLatch(1);
    Thread shutdownThread =
        new Thread(
            () -> {
              reader.shutdown();
              shutdownDone.countDown();
            });
    shutdownThread.setDaemon(true);
    shutdownThread.start();

    // Give shutdown() time to reach the flushInProgress.join() wait.
    // Even if this executes before shutdown enters the wait, the assertions below
    // still
    // validate correctness — they just won't exercise the concurrent case.
    Thread.sleep(200);

    // Release the in-flight export
    inflightExportResult.succeed();

    // Shutdown completes
    assertThat(shutdownDone.await(5, TimeUnit.SECONDS)).isTrue();

    // In-flight export succeeded
    flushResult.join(5, TimeUnit.SECONDS);
    assertThat(flushResult.isSuccess()).isTrue();
    // Final shutdown export also ran (in-flight + final = 2)
    assertThat(exportCount.get()).isEqualTo(2);
    // Exporter.shutdown() was not called while the in-flight export was still
    // pending
    assertThat(shutdownCalledWhileExportPending.get()).isFalse();
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
  void periodicExport_SequentialBatches() throws Exception {
    MetricExporter mockExporter = mock(MetricExporter.class);
    when(mockExporter.getAggregationTemporality(any()))
        .thenReturn(AggregationTemporality.CUMULATIVE);
    when(mockExporter.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode batch1Result = new CompletableResultCode();
    CompletableResultCode batch2Result = CompletableResultCode.ofSuccess();

    // Configure mock to return pending for 1st call, success for 2nd
    when(mockExporter.export(any())).thenReturn(batch1Result).thenReturn(batch2Result);

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(mockExporter)
            .setInterval(
                Duration.ofSeconds(Integer.MAX_VALUE)) // Long interval to prevent auto-trigger
            .setMaxExportBatchSize(3)
            .build();
    // Setup metrics that will result in 2 batches (we have 6 points in
    // LONG_POINT_LIST)
    when(collectionRegistration.collectAllMetrics())
        .thenReturn(Collections.singletonList(METRIC_DATA));
    reader.register(collectionRegistration);

    // Trigger manual flush
    CompletableResultCode flushResult = reader.forceFlush();
    // Verify that the first batch WAS exported
    verify(mockExporter, times(1)).export(any());
    // At this point, batch 1 is stuck waiting. Batch 2 should NOT be exported yet.
    // We verify that export was only called once in total so far.
    verify(mockExporter, times(1)).export(any());
    // Now we complete the first batch
    batch1Result.succeed();
    // Verify that the second batch IS NOW exported
    verify(mockExporter, times(2)).export(any());
    // Ensure the flush operation completes successfully
    assertThat(flushResult.join(5, TimeUnit.SECONDS).isSuccess()).isTrue();
    reader.shutdown();
  }

  @Test
  @SuppressLogger(PeriodicMetricReader.class)
  void periodicExport_SequentialBatches_PartialFailure() throws Exception {
    MetricExporter mockExporter = mock(MetricExporter.class);
    when(mockExporter.getAggregationTemporality(any()))
        .thenReturn(AggregationTemporality.CUMULATIVE);
    when(mockExporter.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode batch1Result = new CompletableResultCode();
    CompletableResultCode batch2Result = new CompletableResultCode();
    CompletableResultCode batch3Result = new CompletableResultCode();

    when(mockExporter.export(any()))
        .thenReturn(batch1Result)
        .thenReturn(batch2Result)
        .thenReturn(batch3Result);

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(mockExporter)
            .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
            .setMaxExportBatchSize(2) // 6 points / 2 = 3 batches
            .build();

    when(collectionRegistration.collectAllMetrics())
        .thenReturn(Collections.singletonList(METRIC_DATA));
    reader.register(collectionRegistration);

    CompletableResultCode flushResult = reader.forceFlush();

    verify(mockExporter, times(1)).export(any());

    batch1Result.succeed();
    verify(mockExporter, times(2)).export(any());

    batch2Result.fail();
    verify(mockExporter, times(3)).export(any());

    batch3Result.succeed();

    // Failed export results are logged, but forceFlush preserves the prior
    // partial-success
    // behavior.
    assertThat(flushResult.join(5, TimeUnit.SECONDS).isSuccess()).isTrue();

    logCapturer.assertContains("Exporter failed");

    reader.shutdown();
  }

  @Test
  void periodicExport_SequentialBatches_PurelySynchronous() throws Exception {
    MetricExporter mockExporter = mock(MetricExporter.class);
    when(mockExporter.getAggregationTemporality(any()))
        .thenReturn(AggregationTemporality.CUMULATIVE);
    when(mockExporter.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    when(mockExporter.export(any()))
        .thenReturn(CompletableResultCode.ofSuccess())
        .thenReturn(CompletableResultCode.ofSuccess())
        .thenReturn(CompletableResultCode.ofSuccess());

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(mockExporter)
            .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
            .setMaxExportBatchSize(2) // 6 points / 2 = 3 batches
            .build();

    when(collectionRegistration.collectAllMetrics())
        .thenReturn(Collections.singletonList(METRIC_DATA));
    reader.register(collectionRegistration);

    CompletableResultCode flushResult = reader.forceFlush();

    // Verify that all 3 batches WERE exported immediately
    verify(mockExporter, times(3)).export(any());

    assertThat(flushResult.join(5, TimeUnit.SECONDS).isSuccess()).isTrue();

    reader.shutdown();
  }

  @Test
  void periodicExport_SequentialBatches_PurelyAsynchronous() throws Exception {
    MetricExporter mockExporter = mock(MetricExporter.class);
    when(mockExporter.getAggregationTemporality(any()))
        .thenReturn(AggregationTemporality.CUMULATIVE);
    when(mockExporter.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode batch1Result = new CompletableResultCode();
    CompletableResultCode batch2Result = new CompletableResultCode();
    CompletableResultCode batch3Result = new CompletableResultCode();

    when(mockExporter.export(any()))
        .thenReturn(batch1Result)
        .thenReturn(batch2Result)
        .thenReturn(batch3Result);

    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(mockExporter)
            .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
            .setMaxExportBatchSize(2) // 6 points / 2 = 3 batches
            .build();

    when(collectionRegistration.collectAllMetrics())
        .thenReturn(Collections.singletonList(METRIC_DATA));
    reader.register(collectionRegistration);

    CompletableResultCode flushResult = reader.forceFlush();

    verify(mockExporter, times(1)).export(any());

    batch1Result.succeed();
    verify(mockExporter, times(2)).export(any());

    batch2Result.succeed();
    verify(mockExporter, times(3)).export(any());

    batch3Result.succeed();

    assertThat(flushResult.join(5, TimeUnit.SECONDS).isSuccess()).isTrue();
    logCapturer.assertDoesNotContain("Exporter failed");

    reader.shutdown();
  }

  @Test
  void stringRepresentation() {
    when(metricExporter.toString()).thenReturn("MockMetricExporter{}");
    assertThat(
            PeriodicMetricReader.builder(metricExporter)
                .setInterval(Duration.ofSeconds(1))
                .setMaxExportBatchSize(200)
                .build()
                .toString())
        .isEqualTo(
            "PeriodicMetricReader{"
                + "exporter=MockMetricExporter{}, "
                + "intervalNanos=1000000000, "
                + "maxExportBatchSize=200"
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
