/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.assertj.LogAssertions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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

@SuppressWarnings("PreferJavaTimeOverload")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BatchLogRecordProcessorTest {

  private static final String LOG_MESSAGE_1 = "Hello world 1!";
  private static final String LOG_MESSAGE_2 = "Hello world 2!";
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;

  @Mock private LogRecordExporter mockLogRecordExporter;

  @BeforeEach
  void setUp() {
    when(mockLogRecordExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  private void emitLog(SdkLoggerProvider sdkLoggerProvider, String message) {
    sdkLoggerProvider
        .loggerBuilder(getClass().getName())
        .build()
        .logRecordBuilder()
        .setBody(message)
        .emit();
  }

  @Test
  void builderDefaults() {
    BatchLogRecordProcessorBuilder builder = BatchLogRecordProcessor.builder(mockLogRecordExporter);
    assertThat(builder.getScheduleDelayNanos())
        .isEqualTo(
            TimeUnit.MILLISECONDS.toNanos(
                BatchLogRecordProcessorBuilder.DEFAULT_SCHEDULE_DELAY_MILLIS));
    assertThat(builder.getMaxQueueSize())
        .isEqualTo(BatchLogRecordProcessorBuilder.DEFAULT_MAX_QUEUE_SIZE);
    assertThat(builder.getMaxExportBatchSize())
        .isEqualTo(BatchLogRecordProcessorBuilder.DEFAULT_MAX_EXPORT_BATCH_SIZE);
    assertThat(builder.getExporterTimeoutNanos())
        .isEqualTo(
            TimeUnit.MILLISECONDS.toNanos(
                BatchLogRecordProcessorBuilder.DEFAULT_EXPORT_TIMEOUT_MILLIS));
  }

  @Test
  void builderInvalidConfig() {
    assertThatThrownBy(
            () ->
                BatchLogRecordProcessor.builder(mockLogRecordExporter)
                    .setScheduleDelay(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("delay must be non-negative");
    assertThatThrownBy(
            () -> BatchLogRecordProcessor.builder(mockLogRecordExporter).setScheduleDelay(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(
            () -> BatchLogRecordProcessor.builder(mockLogRecordExporter).setScheduleDelay(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("delay");
    assertThatThrownBy(
            () ->
                BatchLogRecordProcessor.builder(mockLogRecordExporter)
                    .setExporterTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(
            () ->
                BatchLogRecordProcessor.builder(mockLogRecordExporter).setExporterTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(
            () -> BatchLogRecordProcessor.builder(mockLogRecordExporter).setExporterTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");
  }

  @Test
  void emitMultipleLogs() {
    WaitingLogRecordExporter waitingLogRecordExporter =
        new WaitingLogRecordExporter(2, CompletableResultCode.ofSuccess());
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(waitingLogRecordExporter)
                    .setScheduleDelay(MAX_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .build())
            .build();

    emitLog(loggerProvider, LOG_MESSAGE_1);
    emitLog(loggerProvider, LOG_MESSAGE_2);
    List<LogRecordData> exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported)
        .satisfiesExactly(
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_1),
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_2));
  }

  @Test
  void emitMoreLogsThanBufferSize() {
    CompletableLogRecordExporter logRecordExporter = new CompletableLogRecordExporter();

    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(logRecordExporter)
                    .setMaxQueueSize(6)
                    .setMaxExportBatchSize(2)
                    .setScheduleDelay(MAX_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .build())
            .build();

    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);

    logRecordExporter.succeed();

    await()
        .untilAsserted(
            () ->
                assertThat(logRecordExporter.getExported())
                    .hasSize(6)
                    .allSatisfy(logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_1)));
  }

  @Test
  void forceEmit() {
    WaitingLogRecordExporter waitingLogRecordExporter =
        new WaitingLogRecordExporter(100, CompletableResultCode.ofSuccess(), 1);
    BatchLogRecordProcessor batchLogRecordProcessor =
        BatchLogRecordProcessor.builder(waitingLogRecordExporter)
            .setMaxQueueSize(10_000)
            // Force flush should send all logs, make sure the number of logs we check here is
            // not divisible by the batch size.
            .setMaxExportBatchSize(49)
            .setScheduleDelay(10, TimeUnit.SECONDS)
            .build();

    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(batchLogRecordProcessor).build();
    for (int i = 0; i < 50; i++) {
      emitLog(sdkLoggerProvider, "notExported");
    }
    List<LogRecordData> exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported.size()).isEqualTo(49);

    for (int i = 0; i < 50; i++) {
      emitLog(sdkLoggerProvider, "notExported");
    }
    exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported.size()).isEqualTo(49);

    batchLogRecordProcessor.forceFlush().join(10, TimeUnit.SECONDS);
    exported = waitingLogRecordExporter.getExported();
    assertThat(exported).isNotNull();
    assertThat(exported.size()).isEqualTo(2);
  }

  @Test
  void emitLogsToMultipleExporters() {
    WaitingLogRecordExporter waitingLogRecordExporter1 =
        new WaitingLogRecordExporter(2, CompletableResultCode.ofSuccess());
    WaitingLogRecordExporter waitingLogRecordExporter2 =
        new WaitingLogRecordExporter(2, CompletableResultCode.ofSuccess());
    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(
                        LogRecordExporter.composite(
                            Arrays.asList(waitingLogRecordExporter1, waitingLogRecordExporter2)))
                    .setScheduleDelay(MAX_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .build())
            .build();

    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    emitLog(sdkLoggerProvider, LOG_MESSAGE_2);
    List<LogRecordData> exported1 = waitingLogRecordExporter1.waitForExport();
    List<LogRecordData> exported2 = waitingLogRecordExporter2.waitForExport();
    assertThat(exported1)
        .hasSize(2)
        .satisfiesExactly(
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_1),
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_2));
    assertThat(exported2)
        .hasSize(2)
        .satisfiesExactly(
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_1),
            logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_2));
  }

  @Test
  void emitMoreLogsThanTheMaximumLimit() {
    int maxQueuedLogs = 8;
    BlockingLogRecordExporter blockingLogRecordExporter = new BlockingLogRecordExporter();
    WaitingLogRecordExporter waitingLogRecordExporter =
        new WaitingLogRecordExporter(maxQueuedLogs, CompletableResultCode.ofSuccess());
    SdkLoggerProvider sdkTracerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(
                        LogRecordExporter.composite(
                            Arrays.asList(blockingLogRecordExporter, waitingLogRecordExporter)))
                    .setScheduleDelay(MAX_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .setMaxQueueSize(maxQueuedLogs)
                    .setMaxExportBatchSize(maxQueuedLogs / 2)
                    .build())
            .build();

    // Wait to block the worker thread in the BatchLogRecordProcessor. This ensures that no items
    // can be removed from the queue. Need to add a log to trigger the export otherwise the
    // pipeline is never called.
    emitLog(sdkTracerProvider, "blocking log");
    blockingLogRecordExporter.waitUntilIsBlocked();

    for (int i = 0; i < maxQueuedLogs; i++) {
      // First export maxQueuedLogs, the worker thread is blocked so all items should be queued.
      emitLog(sdkTracerProvider, "log_1_" + 1);
    }

    // Now we should start dropping.
    for (int i = 0; i < 7; i++) {
      emitLog(sdkTracerProvider, "log_2_" + i);
    }

    // Release the blocking exporter
    blockingLogRecordExporter.unblock();

    // While we wait for maxQueuedLogs we ensure that the queue is also empty after this.
    List<LogRecordData> exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported).hasSize(maxQueuedLogs + 1);

    // Clear, reset, add another batch of logs, and confirm they are exported
    exported.clear();
    waitingLogRecordExporter.reset();
    for (int i = 0; i < maxQueuedLogs; i++) {
      emitLog(sdkTracerProvider, "log_3_" + i);
    }

    exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported).hasSize(maxQueuedLogs);
  }

  @Test
  void ignoresNullLogs() {
    BatchLogRecordProcessor processor =
        BatchLogRecordProcessor.builder(mockLogRecordExporter).build();
    try {
      assertThatCode(() -> processor.onEmit(null, null)).doesNotThrowAnyException();
    } finally {
      processor.shutdown();
    }
  }

  @Test
  @SuppressLogger(MultiLogRecordExporter.class)
  void exporterThrowsException() {
    WaitingLogRecordExporter waitingLogRecordExporter =
        new WaitingLogRecordExporter(1, CompletableResultCode.ofSuccess());
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockLogRecordExporter)
        .export(anyList());
    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(
                        LogRecordExporter.composite(
                            Arrays.asList(mockLogRecordExporter, waitingLogRecordExporter)))
                    .setScheduleDelay(MAX_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .build())
            .build();

    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);

    List<LogRecordData> exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported)
        .satisfiesExactly(logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_1));
    waitingLogRecordExporter.reset();
    // Continue to export after the exception was received.
    emitLog(sdkLoggerProvider, LOG_MESSAGE_2);
    exported = waitingLogRecordExporter.waitForExport();
    assertThat(exported)
        .satisfiesExactly(logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_2));
  }

  @Test
  @Timeout(5)
  public void continuesIfExporterTimesOut() throws InterruptedException {
    int exporterTimeoutMillis = 10;
    BatchLogRecordProcessor blp =
        BatchLogRecordProcessor.builder(mockLogRecordExporter)
            .setExporterTimeout(exporterTimeoutMillis, TimeUnit.MILLISECONDS)
            .setScheduleDelay(1, TimeUnit.MILLISECONDS)
            .setMaxQueueSize(1)
            .build();
    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(blp).build();

    CountDownLatch exported = new CountDownLatch(1);
    // We return a result we never complete, meaning it will timeout.
    when(mockLogRecordExporter.export(
            argThat(
                logs -> {
                  assertThat(logs)
                      .anySatisfy(log -> LogAssertions.assertThat(log).hasBody(LOG_MESSAGE_1));
                  exported.countDown();
                  return true;
                })))
        .thenReturn(new CompletableResultCode());
    emitLog(sdkLoggerProvider, LOG_MESSAGE_1);
    exported.await();
    // Timed out so the log was dropped.
    await().untilAsserted(() -> assertThat(blp.getBatch()).isEmpty());

    // Still processing new logs.
    CountDownLatch exportedAgain = new CountDownLatch(1);
    reset(mockLogRecordExporter);
    when(mockLogRecordExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(mockLogRecordExporter.export(
            argThat(
                logs -> {
                  assertThat(logs)
                      .anySatisfy(log -> LogAssertions.assertThat(log).hasBody(LOG_MESSAGE_2));
                  exportedAgain.countDown();
                  return true;
                })))
        .thenReturn(CompletableResultCode.ofSuccess());
    emitLog(sdkLoggerProvider, LOG_MESSAGE_2);
    exported.await();
    await().untilAsserted(() -> assertThat(blp.getBatch()).isEmpty());
  }

  @Test
  @Timeout(10)
  void shutdownFlushes() {
    WaitingLogRecordExporter waitingLogRecordExporter =
        new WaitingLogRecordExporter(1, CompletableResultCode.ofSuccess());
    // Set the export delay to large value, in order to confirm the #flush() below works

    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(waitingLogRecordExporter)
                    .setScheduleDelay(10, TimeUnit.SECONDS)
                    .build())
            .build();

    emitLog(sdkLoggerProvider, LOG_MESSAGE_2);

    // Force a shutdown, which forces processing of all remaining logs.
    sdkLoggerProvider.shutdown().join(10, TimeUnit.SECONDS);

    List<LogRecordData> exported = waitingLogRecordExporter.getExported();
    assertThat(exported)
        .satisfiesExactly(logRecordData -> assertThat(logRecordData).hasBody(LOG_MESSAGE_2));
    assertThat(waitingLogRecordExporter.shutDownCalled.get()).isTrue();
  }

  @Test
  void shutdownPropagatesSuccess() {
    BatchLogRecordProcessor processor =
        BatchLogRecordProcessor.builder(mockLogRecordExporter).build();
    CompletableResultCode result = processor.shutdown();
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void shutdownPropagatesFailure() {
    when(mockLogRecordExporter.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    BatchLogRecordProcessor processor =
        BatchLogRecordProcessor.builder(mockLogRecordExporter).build();
    CompletableResultCode result = processor.shutdown();
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  void toString_Valid() {
    when(mockLogRecordExporter.toString()).thenReturn("MockLogRecordExporter");
    assertThat(BatchLogRecordProcessor.builder(mockLogRecordExporter).build().toString())
        .isEqualTo(
            "BatchLogRecordProcessor{logRecordExporter=MockLogRecordExporter, scheduleDelayNanos=200000000, maxExportBatchSize=512, exporterTimeoutNanos=30000000000}");
  }

  private static final class BlockingLogRecordExporter implements LogRecordExporter {

    final Object monitor = new Object();

    private enum State {
      WAIT_TO_BLOCK,
      BLOCKED,
      UNBLOCKED
    }

    @GuardedBy("monitor")
    State state = State.WAIT_TO_BLOCK;

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
      synchronized (monitor) {
        while (state != State.UNBLOCKED) {
          try {
            state = State.BLOCKED;
            // Some threads may wait for Blocked State.
            monitor.notifyAll();
            monitor.wait();
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    private void waitUntilIsBlocked() {
      synchronized (monitor) {
        while (state != State.BLOCKED) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
    }

    @Override
    public CompletableResultCode shutdown() {
      // Do nothing;
      return CompletableResultCode.ofSuccess();
    }

    private void unblock() {
      synchronized (monitor) {
        state = State.UNBLOCKED;
        monitor.notifyAll();
      }
    }
  }

  private static class CompletableLogRecordExporter implements LogRecordExporter {

    private final List<CompletableResultCode> results = new ArrayList<>();

    private final List<LogRecordData> exported = new ArrayList<>();

    private volatile boolean succeeded;

    List<LogRecordData> getExported() {
      return exported;
    }

    void succeed() {
      succeeded = true;
      results.forEach(CompletableResultCode::succeed);
    }

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
      exported.addAll(logs);
      if (succeeded) {
        return CompletableResultCode.ofSuccess();
      }
      CompletableResultCode result = new CompletableResultCode();
      results.add(result);
      return result;
    }

    @Override
    public CompletableResultCode flush() {
      if (succeeded) {
        return CompletableResultCode.ofSuccess();
      } else {
        return CompletableResultCode.ofFailure();
      }
    }

    @Override
    public CompletableResultCode shutdown() {
      return flush();
    }
  }

  static class WaitingLogRecordExporter implements LogRecordExporter {

    private final List<LogRecordData> logRecordDataList = new ArrayList<>();
    private final int numberToWaitFor;
    private final CompletableResultCode exportResultCode;
    private CountDownLatch countDownLatch;
    private int timeout = 10;
    private final AtomicBoolean shutDownCalled = new AtomicBoolean(false);

    WaitingLogRecordExporter(int numberToWaitFor, CompletableResultCode exportResultCode) {
      countDownLatch = new CountDownLatch(numberToWaitFor);
      this.numberToWaitFor = numberToWaitFor;
      this.exportResultCode = exportResultCode;
    }

    WaitingLogRecordExporter(
        int numberToWaitFor, CompletableResultCode exportResultCode, int timeout) {
      this(numberToWaitFor, exportResultCode);
      this.timeout = timeout;
    }

    List<LogRecordData> getExported() {
      List<LogRecordData> result = new ArrayList<>(logRecordDataList);
      logRecordDataList.clear();
      return result;
    }

    /**
     * Waits until we received {@link #numberToWaitFor} logs to export. Returns the list of exported
     * {@link LogRecordData} objects, otherwise {@code null} if the current thread is interrupted.
     *
     * @return the list of exported {@link LogRecordData} objects, otherwise {@code null} if the
     *     current thread is interrupted.
     */
    @Nullable
    List<LogRecordData> waitForExport() {
      try {
        countDownLatch.await(timeout, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        // Preserve the interruption status as per guidance.
        Thread.currentThread().interrupt();
        return null;
      }
      return getExported();
    }

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
      this.logRecordDataList.addAll(logs);
      for (int i = 0; i < logs.size(); i++) {
        countDownLatch.countDown();
      }
      return exportResultCode;
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      shutDownCalled.set(true);
      return CompletableResultCode.ofSuccess();
    }

    public void reset() {
      this.countDownLatch = new CountDownLatch(numberToWaitFor);
    }
  }
}
