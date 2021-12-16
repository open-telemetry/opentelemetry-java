/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link LogProcessor} that batches logs exported by the SDK then pushes them
 * to the exporter pipeline.
 *
 * <p>All logs reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full logs are dropped). Logs are exported either
 * when there are {@code maxExportBatchSize} pending logs or {@code scheduleDelayNanos} has passed
 * since the last export finished.
 */
public final class BatchLogProcessor implements LogProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchLogProcessor.class.getSimpleName() + "_WorkerThread";
  private static final AttributeKey<String> LOG_PROCESSOR_TYPE_LABEL =
      AttributeKey.stringKey("logProcessorType");
  private static final AttributeKey<Boolean> LOG_PROCESSOR_DROPPED_LABEL =
      AttributeKey.booleanKey("dropped");
  private static final String LOG_PROCESSOR_TYPE_VALUE = BatchLogProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchLogProcessor}.
   *
   * @param logExporter the {@link LogExporter} to which the Logs are pushed
   * @return a new {@link BatchLogProcessorBuilder}.
   * @throws NullPointerException if the {@code logExporter} is {@code null}.
   */
  public static BatchLogProcessorBuilder builder(LogExporter logExporter) {
    return new BatchLogProcessorBuilder(logExporter);
  }

  BatchLogProcessor(
      LogExporter logExporter,
      MeterProvider meterProvider,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.worker =
        new Worker(
            logExporter,
            meterProvider,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize)); // TODO: use JcTools.newFixedSizeQueue(..)
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
  }

  @Override
  public void emit(LogData logData) {
    if (logData == null) {
      return;
    }
    worker.addLog(logData);
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    return worker.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return worker.forceFlush();
  }

  // Visible for testing
  ArrayList<LogData> getBatch() {
    return worker.batch;
  }

  // Worker is a thread that batches multiple logs and calls the registered LogExporter to export
  // the data.
  private static final class Worker implements Runnable {

    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    private final LongCounter processedLogsCounter;
    private final Attributes droppedAttrs;
    private final Attributes exportedAttrs;

    private final LogExporter logExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<LogData> queue;
    // When waiting on the logs queue, exporter thread sets this atomic to the number of more
    // logs it needs before doing an export. Writer threads would then wait for the queue to reach
    // logsNeeded size before notifying the exporter thread about new entries.
    // Integer.MAX_VALUE is used to imply that exporter thread is not expecting any signal. Since
    // exporter thread doesn't expect any signal initially, this value is initialized to
    // Integer.MAX_VALUE.
    private final AtomicInteger logsNeeded = new AtomicInteger(Integer.MAX_VALUE);
    private final BlockingQueue<Boolean> signal;
    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private final ArrayList<LogData> batch;

    private Worker(
        LogExporter logExporter,
        MeterProvider meterProvider,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        Queue<LogData> queue) {
      this.logExporter = logExporter;
      this.scheduleDelayNanos = scheduleDelayNanos;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutNanos = exporterTimeoutNanos;
      this.queue = queue;
      this.signal = new ArrayBlockingQueue<>(1);
      Meter meter = meterProvider.meterBuilder("io.opentelemetry.sdk.logs").build();
      meter
          .gaugeBuilder("queueSize")
          .ofLongs()
          .setDescription("The number of logs queued")
          .setUnit("1")
          .buildWithCallback(
              result ->
                  result.record(
                      queue.size(),
                      Attributes.of(LOG_PROCESSOR_TYPE_LABEL, LOG_PROCESSOR_TYPE_VALUE)));
      processedLogsCounter =
          meter
              .counterBuilder("processedLogs")
              .setUnit("1")
              .setDescription(
                  "The number of logs processed by the BatchLogProcessor. "
                      + "[dropped=true if they were dropped due to high throughput]")
              .build();
      droppedAttrs =
          Attributes.of(
              LOG_PROCESSOR_TYPE_LABEL,
              LOG_PROCESSOR_TYPE_VALUE,
              LOG_PROCESSOR_DROPPED_LABEL,
              true);
      exportedAttrs =
          Attributes.of(
              LOG_PROCESSOR_TYPE_LABEL,
              LOG_PROCESSOR_TYPE_VALUE,
              LOG_PROCESSOR_DROPPED_LABEL,
              false);

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addLog(LogData logData) {
      if (!queue.offer(logData)) {
        processedLogsCounter.add(1, droppedAttrs);
      } else {
        if (queue.size() >= logsNeeded.get()) {
          signal.offer(true);
        }
      }
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        if (flushRequested.get() != null) {
          flush();
        }
        while (!queue.isEmpty() && batch.size() < maxExportBatchSize) {
          batch.add(queue.poll());
        }
        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
          exportCurrentBatch();
          updateNextExportTime();
        }
        if (queue.isEmpty()) {
          try {
            long pollWaitTime = nextExportTime - System.nanoTime();
            if (pollWaitTime > 0) {
              logsNeeded.set(maxExportBatchSize - batch.size());
              signal.poll(pollWaitTime, TimeUnit.NANOSECONDS);
              logsNeeded.set(Integer.MAX_VALUE);
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }

    private void flush() {
      int logsToFlush = queue.size();
      while (logsToFlush > 0) {
        LogData logData = queue.poll();
        assert logData != null;
        batch.add(logData);
        logsToFlush--;
        if (batch.size() >= maxExportBatchSize) {
          exportCurrentBatch();
        }
      }
      exportCurrentBatch();
      CompletableResultCode flushResult = flushRequested.get();
      if (flushResult != null) {
        flushResult.succeed();
        flushRequested.set(null);
      }
    }

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    private CompletableResultCode shutdown() {
      final CompletableResultCode result = new CompletableResultCode();

      final CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(
          () -> {
            continueWork = false;
            final CompletableResultCode shutdownResult = logExporter.shutdown();
            shutdownResult.whenComplete(
                () -> {
                  if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
                    result.fail();
                  } else {
                    result.succeed();
                  }
                });
          });

      return result;
    }

    private CompletableResultCode forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      // we set the atomic here to trigger the worker loop to do a flush of the entire queue.
      if (flushRequested.compareAndSet(null, flushResult)) {
        signal.offer(true);
      }
      CompletableResultCode possibleResult = flushRequested.get();
      // there's a race here where the flush happening in the worker loop could complete before we
      // get what's in the atomic. In that case, just return success, since we know it succeeded in
      // the interim.
      return possibleResult == null ? CompletableResultCode.ofSuccess() : possibleResult;
    }

    private void exportCurrentBatch() {
      if (batch.isEmpty()) {
        return;
      }

      try {
        final CompletableResultCode result =
            logExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (result.isSuccess()) {
          processedLogsCounter.add(batch.size(), exportedAttrs);
        } else {
          logger.log(Level.FINE, "Exporter failed");
        }
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Exporter threw an Exception", e);
      } finally {
        batch.clear();
      }
    }
  }
}
