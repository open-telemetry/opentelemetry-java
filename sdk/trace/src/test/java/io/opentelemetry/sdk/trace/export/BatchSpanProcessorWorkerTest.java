/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.JcTools;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(30)
class BatchSpanProcessorWorkerTest {
  private final CountDownLatch emptyChecked = new CountDownLatch(1);
  private final CountDownLatch resumeWorker = new CountDownLatch(1);
  private final CountDownLatch emptyAfterWake = new CountDownLatch(1);
  private final AtomicBoolean observeEmptyAfterWake = new AtomicBoolean();
  private final AtomicBoolean delayShutdownCallback = new AtomicBoolean();
  private final AtomicBoolean pauseBeforeWait = new AtomicBoolean(true);
  private final BlockingQueue<List<SpanData>> exports = new LinkedBlockingQueue<>();
  private final SdkTracerProvider tracerProvider =
      SdkTracerProvider.builder()
          .addSpanProcessor(SimpleSpanProcessor.create(SpanExporter.noop()))
          .build();
  private BatchSpanProcessor.Worker worker;
  private Thread workerThread;

  @BeforeEach
  void setUp() {
    Queue<ReadableSpan> queue = spy(JcTools.<ReadableSpan>newFixedSizeQueue(16));
    doAnswer(
            invocation -> {
              boolean empty = (boolean) invocation.callRealMethod();
              if (empty && pauseBeforeWait.compareAndSet(true, false)) {
                emptyChecked.countDown();
                assertThat(resumeWorker.await(5, TimeUnit.SECONDS)).isTrue();
              }
              if (empty && observeEmptyAfterWake.compareAndSet(true, false)) {
                emptyAfterWake.countDown();
              }
              return empty;
            })
        .when(queue)
        .isEmpty();
    SpanExporter exporter = mock(SpanExporter.class);
    when(exporter.export(anyList()))
        .thenAnswer(invocation -> {
          exports.add(new ArrayList<>(invocation.<List<SpanData>>getArgument(0)));
          return CompletableResultCode.ofSuccess();
        });
    when(exporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    worker =
        new BatchSpanProcessor.Worker(
            exporter,
            MeterProvider::noop,
            InternalTelemetryVersion.LEGACY,
            TimeUnit.HOURS.toNanos(1),
            2,
            TimeUnit.SECONDS.toNanos(5),
            queue,
            16) {
          @Override
          CompletableResultCode forceFlush() {
            CompletableResultCode flush = super.forceFlush();
            if (delayShutdownCallback.get()) {
              resumeWorker.countDown();
              flush.join(5, TimeUnit.SECONDS);
              assertThat(flush.isSuccess()).isTrue();
              // Let the worker consume the flush signal and sleep before shutdown registers its
              // callback, which will consequently run on the shutdown caller.
              awaitWorkerPark();
            }
            return flush;
          }
        };
    workerThread = new Thread(worker, "batch-span-processor-test");
    workerThread.setDaemon(true);
    workerThread.start();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    resumeWorker.countDown();
    workerThread.interrupt();
    workerThread.join(5_000);
    assertThat(workerThread.isAlive()).isFalse();
    tracerProvider.close();
  }

  @Test
  void batchArrivingBeforeWaitIsExportedWithoutWaitingForSchedule() throws InterruptedException {
    assertThat(emptyChecked.await(5, TimeUnit.SECONDS)).isTrue();
    worker.addSpan(span("first"));
    worker.addSpan(span("second"));

    resumeWorker.countDown();

    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("first", "second");
  }

  @Test
  void flushRequestedBeforeWaitExportsPartialBatch() throws InterruptedException {
    assertThat(emptyChecked.await(5, TimeUnit.SECONDS)).isTrue();
    worker.addSpan(span("partial"));
    CompletableResultCode flush = worker.forceFlush();

    resumeWorker.countDown();

    flush.join(5, TimeUnit.SECONDS);
    assertThat(flush.isSuccess()).isTrue();
    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("partial");
  }

  @Test
  void shutdownBeforeWaitFlushesAndStopsWorker() throws InterruptedException {
    assertThat(emptyChecked.await(5, TimeUnit.SECONDS)).isTrue();
    worker.addSpan(span("last"));
    CompletableResultCode shutdown = worker.shutdown();

    resumeWorker.countDown();

    shutdown.join(5, TimeUnit.SECONDS);
    assertThat(shutdown.isSuccess()).isTrue();
    workerThread.join(5_000);
    assertThat(workerThread.isAlive()).isFalse();
    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("last");
  }

  @Test
  void shutdownWakesWorkerWhenFlushCompletedBeforeCallbackRegistration()
      throws InterruptedException {
    assertThat(emptyChecked.await(5, TimeUnit.SECONDS)).isTrue();
    delayShutdownCallback.set(true);

    CompletableResultCode shutdown = worker.shutdown();

    shutdown.join(5, TimeUnit.SECONDS);
    assertThat(shutdown.isSuccess()).isTrue();
    workerThread.join(5_000);
    assertThat(workerThread.isAlive()).isFalse();
  }

  @Test
  void fullBatchWakesWaitingWorker() throws InterruptedException {
    waitUntilParked();

    worker.addSpan(span("first"));
    worker.addSpan(span("second"));

    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("first", "second");
  }

  @Test
  void flushWakesWaitingWorkerForPartialBatch() throws InterruptedException {
    waitUntilParked();
    worker.addSpan(span("partial"));

    CompletableResultCode flush = worker.forceFlush();

    flush.join(5, TimeUnit.SECONDS);
    assertThat(flush.isSuccess()).isTrue();
    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("partial");
  }

  @Test
  void spuriousWakeupDoesNotPreventSubsequentExport() throws InterruptedException {
    waitUntilParked();
    observeEmptyAfterWake.set(true);
    LockSupport.unpark(workerThread);
    assertThat(emptyAfterWake.await(5, TimeUnit.SECONDS)).isTrue();
    awaitWorkerPark();

    worker.addSpan(span("first"));
    worker.addSpan(span("second"));

    assertThat(exports.poll(5, TimeUnit.SECONDS))
        .isNotNull()
        .extracting(SpanData::getName)
        .containsExactly("first", "second");
  }

  private void waitUntilParked() throws InterruptedException {
    assertThat(emptyChecked.await(5, TimeUnit.SECONDS)).isTrue();
    resumeWorker.countDown();
    awaitWorkerPark();
  }

  private void awaitWorkerPark() {
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(LockSupport.getBlocker(workerThread)).isSameAs(worker));
  }

  private ReadableSpan span(String name) {
    Span span = tracerProvider.get("test").spanBuilder(name).startSpan();
    span.end();
    return (ReadableSpan) span;
  }
}
