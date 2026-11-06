/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleLogRecordProcessorTest {

  private static final LogRecordData LOG_RECORD_DATA = TestLogRecordData.builder().build();
  private static final int CONCURRENT_ITERATIONS = 100;
  private static final int CONCURRENT_EXPORTS = 16;

  @Mock private LogRecordExporter logRecordExporter;
  @Mock private ReadWriteLogRecord readWriteLogRecord;

  private LogRecordProcessor logRecordProcessor;

  @BeforeEach
  void setUp() {
    logRecordProcessor = SimpleLogRecordProcessor.create(logRecordExporter);
    when(logRecordExporter.export(anyCollection())).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(readWriteLogRecord.toLogRecordData()).thenReturn(LOG_RECORD_DATA);
  }

  @Test
  void create_NullExporter() {
    assertThatThrownBy(() -> SimpleLogRecordProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void onEmit() {
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    verify(logRecordExporter).export(Collections.singletonList(LOG_RECORD_DATA));
  }

  @Test
  @SuppressLogger(SimpleLogRecordProcessor.class)
  void onEmit_ExporterError() {
    when(logRecordExporter.export(any())).thenThrow(new RuntimeException("Exporter error!"));
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    verify(logRecordExporter, times(2)).export(anyList());
  }

  @Test
  @SuppressLogger(SimpleLogRecordProcessor.class)
  void onEmit_ExporterThrowsCheckedException() {
    when(logRecordExporter.export(any()))
        .thenAnswer(
            invocation -> {
              throw new Exception("Exporter error!");
            });

    assertThatCode(() -> logRecordProcessor.onEmit(Context.current(), readWriteLogRecord))
        .doesNotThrowAnyException();
    verify(logRecordExporter).export(anyList());
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logRecordExporter.export(any())).thenReturn(export1, export2);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    verify(logRecordExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode flush = logRecordProcessor.forceFlush();
    assertThat(flush.isDone()).isFalse();

    export1.succeed();
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_NoPendingExports() {
    CompletableResultCode flush = logRecordProcessor.forceFlush();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_FailedExport() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logRecordExporter.export(any())).thenReturn(export1, export2);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    CompletableResultCode flush = logRecordProcessor.forceFlush();
    RuntimeException error = new RuntimeException("export failed");
    export1.failExceptionally(error);
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isFalse();
    assertThat(flush.getFailureThrowable()).isSameAs(error);
  }

  @Test
  void forceFlush_RepeatedAfterExportsComplete() {
    CompletableResultCode export = new CompletableResultCode();

    when(logRecordExporter.export(any())).thenReturn(export);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    CompletableResultCode flush1 = logRecordProcessor.forceFlush();
    export.succeed();
    assertThat(flush1.isDone()).isTrue();
    assertThat(flush1.isSuccess()).isTrue();

    for (int i = 0; i < 3; i++) {
      CompletableResultCode flush = logRecordProcessor.forceFlush();
      assertThat(flush.isDone()).isTrue();
      assertThat(flush.isSuccess()).isTrue();
    }
    assertThat(flush1.isSuccess()).isTrue();
  }

  // Regression test for https://github.com/open-telemetry/opentelemetry-java/issues/8995: exports
  // completing while forceFlush() collects them must not leave the flush result incomplete. The
  // exact interleaving cannot be forced without production test hooks, so race a bounded number of
  // times. Once every export has completed, a correct flush result is expected to be done already,
  // so the check does not depend on a timeout.
  @Test
  void forceFlush_ExportsCompleteConcurrently() throws InterruptedException {
    for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
      List<CompletableResultCode> exports =
          emitAsyncExports(logRecordProcessor, CONCURRENT_EXPORTS);

      CompletableResultCode flush = callWhileCompleting(exports, logRecordProcessor::forceFlush);

      assertThat(exports).allMatch(CompletableResultCode::isDone);
      assertThat(flush.isDone()).as("flush done in iteration %s", i).isTrue();
      assertThat(flush.isSuccess()).isTrue();
    }
  }

  @Test
  void shutdown_ExportsCompleteConcurrently() throws InterruptedException {
    for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
      LogRecordProcessor processor = SimpleLogRecordProcessor.create(logRecordExporter);
      List<CompletableResultCode> exports = emitAsyncExports(processor, CONCURRENT_EXPORTS);

      CompletableResultCode shutdown = callWhileCompleting(exports, processor::shutdown);

      assertThat(exports).allMatch(CompletableResultCode::isDone);
      assertThat(shutdown.isDone()).as("shutdown done in iteration %s", i).isTrue();
      assertThat(shutdown.isSuccess()).isTrue();
    }
    verify(logRecordExporter, times(CONCURRENT_ITERATIONS)).shutdown();
  }

  @Test
  void shutdown() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logRecordExporter.export(any())).thenReturn(export1, export2);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    verify(logRecordExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode shutdown = logRecordProcessor.shutdown();
    assertThat(shutdown.isDone()).isFalse();

    export1.succeed();
    assertThat(shutdown.isDone()).isFalse();
    verify(logRecordExporter, never()).shutdown();

    export2.succeed();
    assertThat(shutdown.isDone()).isTrue();
    assertThat(shutdown.isSuccess()).isTrue();
    verify(logRecordExporter).shutdown();
  }

  @Test
  void getLogRecordExporter() {
    assertThat(
            ((SimpleLogRecordProcessor) SimpleLogRecordProcessor.create(logRecordExporter))
                .getLogRecordExporter())
        .isSameAs(logRecordExporter);
  }

  @Test
  void toString_Valid() {
    when(logRecordExporter.toString()).thenReturn("MockLogRecordExporter");
    assertThat(logRecordProcessor.toString())
        .isEqualTo("SimpleLogRecordProcessor{logRecordExporter=MockLogRecordExporter}");
  }

  @SuppressWarnings("MockitoDoSetup")
  private List<CompletableResultCode> emitAsyncExports(LogRecordProcessor processor, int count) {
    List<CompletableResultCode> exports = new ArrayList<>();
    // doAnswer avoids invoking the previous answer while re-stubbing, which would record an extra
    // result that was never exported.
    doAnswer(
            invocation -> {
              CompletableResultCode export = new CompletableResultCode();
              exports.add(export);
              return export;
            })
        .when(logRecordExporter)
        .export(any());
    for (int i = 0; i < count; i++) {
      processor.onEmit(Context.current(), readWriteLogRecord);
    }
    return exports;
  }

  /** Invokes {@code call} while another thread completes {@code exports}. */
  private static CompletableResultCode callWhileCompleting(
      List<CompletableResultCode> exports, Supplier<CompletableResultCode> call)
      throws InterruptedException {
    CountDownLatch ready = new CountDownLatch(1);
    CountDownLatch start = new CountDownLatch(1);
    Thread completer =
        new Thread(
            () -> {
              ready.countDown();
              try {
                if (!start.await(10, TimeUnit.SECONDS)) {
                  return;
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }
              exports.forEach(CompletableResultCode::succeed);
            });
    // Daemon, and every wait below is bounded, so a regression fails the test instead of hanging
    // the build. The bounds are failure guards; on a passing run each wait is normally very short.
    completer.setDaemon(true);
    completer.start();
    try {
      assertThat(ready.await(10, TimeUnit.SECONDS)).as("completer thread started").isTrue();
      start.countDown();
      return call.get();
    } finally {
      start.countDown();
      completer.join(TimeUnit.SECONDS.toMillis(10));
      assertThat(completer.isAlive()).as("completer thread finished").isFalse();
    }
  }
}
