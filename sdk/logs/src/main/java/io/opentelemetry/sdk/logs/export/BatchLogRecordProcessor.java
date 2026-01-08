/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link LogRecordProcessor} that batches logs exported by the SDK then
 * pushes them to the exporter pipeline.
 *
 * <p>All logs reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full logs are dropped). Logs are exported either
 * when there are {@code maxExportBatchSize} pending logs or {@code scheduleDelayNanos} has passed
 * since the last export finished.
 *
 * @since 1.27.0
 */
public final class BatchLogRecordProcessor implements LogRecordProcessor {

  private static final ComponentId COMPONENT_ID =
      ComponentId.generateLazy("batching_log_processor");

  private static final String WORKER_THREAD_NAME =
      BatchLogRecordProcessor.class.getSimpleName() + "_WorkerThread";

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchLogRecordProcessor}.
   *
   * @param logRecordExporter the {@link LogRecordExporter} to which the Logs are pushed
   * @return a new {@link BatchLogRecordProcessor}.
   * @throws NullPointerException if the {@code logRecordExporter} is {@code null}.
   */
  public static BatchLogRecordProcessorBuilder builder(LogRecordExporter logRecordExporter) {
    return new BatchLogRecordProcessorBuilder(logRecordExporter);
  }

  BatchLogRecordProcessor(
      LogRecordExporter logRecordExporter,
      Supplier<MeterProvider> meterProvider,
      InternalTelemetryVersion telemetryVersion,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.worker =
        new Worker(
            logRecordExporter,
            meterProvider,
            telemetryVersion,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize),
            maxQueueSize); // TODO: use JcTools.newFixedSizeQueue(..)
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    if (logRecord == null) {
      return;
    }
    worker.addLog(logRecord);
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

  /**
   * Return the processor's configured {@link LogRecordExporter}.
   *
   * @since 1.37.0
   */
  public LogRecordExporter getLogRecordExporter() {
    return worker.logRecordExporter;
  }

  // Visible for testing
  List<LogRecordData> getBatch() {
    return worker.batch;
  }

  @Override
  public String toString() {
    return "BatchLogRecordProcessor{"
        + "logRecordExporter="
        + worker.logRecordExporter
        + ", scheduleDelayNanos="
        + worker.scheduleDelayNanos
        + ", maxExportBatchSize="
        + worker.maxExportBatchSize
        + ", exporterTimeoutNanos="
        + worker.exporterTimeoutNanos
        + '}';
  }

  // Worker is a thread that batches multiple logs and calls the registered LogRecordExporter to
  // export
  // the data.
  private static final class Worker implements Runnable {

    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    private final LogRecordProcessorInstrumentation logProcessorInstrumentation;

    private final LogRecordExporter logRecordExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<ReadWriteLogRecord> queue;
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
    private final ArrayList<LogRecordData> batch;
    private final long maxQueueSize;

    private Worker(
        LogRecordExporter logRecordExporter,
        Supplier<MeterProvider> meterProvider,
        InternalTelemetryVersion telemetryVersion,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        Queue<ReadWriteLogRecord> queue,
        long maxQueueSize) {
      this.logRecordExporter = logRecordExporter;
      this.scheduleDelayNanos = scheduleDelayNanos;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutNanos = exporterTimeoutNanos;
      this.queue = queue;
      this.signal = new ArrayBlockingQueue<>(1);
      logProcessorInstrumentation =
          LogRecordProcessorInstrumentation.get(telemetryVersion, COMPONENT_ID, meterProvider);
      this.maxQueueSize = maxQueueSize;

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addLog(ReadWriteLogRecord logData) {
      logProcessorInstrumentation.buildQueueMetricsOnce(maxQueueSize, queue::size);
      if (!queue.offer(logData)) {
        logProcessorInstrumentation.dropLogs(1);
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
          batch.add(queue.poll().toLogRecordData());
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
        ReadWriteLogRecord logRecord = queue.poll();
        assert logRecord != null;
        batch.add(logRecord.toLogRecordData());
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
      CompletableResultCode result = new CompletableResultCode();

      CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(
          () -> {
            continueWork = false;
            CompletableResultCode shutdownResult = logRecordExporter.shutdown();
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

      String error = null;
      try {
        CompletableResultCode result =
            logRecordExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (!result.isSuccess()) {
          logger.log(Level.FINE, "Exporter failed");
          if (result.getFailureThrowable() != null) {
            error = result.getFailureThrowable().getClass().getName();
          } else {
            error = "export_failed";
          }
        }
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Exporter threw an Exception", e);
        error = e.getClass().getName();
      } finally {
        logProcessorInstrumentation.finishLogs(batch.size(), error);
        batch.clear();
      }
    }
  }
}
