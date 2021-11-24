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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class BatchLogProcessor implements LogProcessor {
  private static final String WORKER_THREAD_NAME =
      BatchLogProcessor.class.getSimpleName() + "_WorkerThread";

  private final Worker worker;
  private final Thread workerThread;

  BatchLogProcessor(
      int maxQueueSize,
      long scheduleDelayMillis,
      int maxExportBatchSize,
      long exporterTimeoutMillis,
      LogExporter logExporter,
      MeterProvider meterProvider) {
    this.worker =
        new Worker(
            logExporter,
            meterProvider,
            scheduleDelayMillis,
            maxExportBatchSize,
            exporterTimeoutMillis,
            new ArrayBlockingQueue<>(maxQueueSize));
    this.workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    this.workerThread.start();
  }

  public static BatchLogProcessorBuilder builder(LogExporter logExporter) {
    return new BatchLogProcessorBuilder(logExporter);
  }

  @Override
  public void emit(LogData logData) {
    worker.addLog(logData);
  }

  @Override
  public CompletableResultCode shutdown() {
    workerThread.interrupt();
    return worker.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return worker.forceFlush();
  }

  private static class Worker implements Runnable {

    private final LongCounter processedLogsCounter;
    private final Attributes failureAttrs;
    private final Attributes queueFullAttrs;
    private final Attributes successAttrs;

    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final LogExporter logExporter;
    private final long exporterTimeoutMillis;
    private final ArrayList<LogData> batch;
    private final BlockingQueue<LogData> queue;

    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private long nextExportTime;

    private Worker(
        LogExporter logExporter,
        MeterProvider meterProvider,
        long scheduleDelayMillis,
        int maxExportBatchSize,
        long exporterTimeoutMillis,
        BlockingQueue<LogData> queue) {
      this.logExporter = logExporter;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      this.scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(scheduleDelayMillis);
      this.queue = queue;
      this.batch = new ArrayList<>(this.maxExportBatchSize);

      // TODO: As of Specification 1.4, this should have a telemetry schema version.
      Meter meter = meterProvider.meterBuilder("io.opentelemetry.sdk.logs").build();
      processedLogsCounter =
          meter
              .counterBuilder("logRecordsProcessed")
              .setUnit("1")
              .setDescription("Number of records processed")
              .build();
      AttributeKey<String> resultKey = AttributeKey.stringKey("result");
      AttributeKey<String> causeKey = AttributeKey.stringKey("cause");
      successAttrs = Attributes.of(resultKey, "success");
      failureAttrs = Attributes.of(resultKey, "dropped record", causeKey, "exporter failure");
      queueFullAttrs = Attributes.of(resultKey, "dropped record", causeKey, "queue full");
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        if (flushRequested.get() != null) {
          flush();
        }

        try {
          LogData lastElement = queue.poll(100, TimeUnit.MILLISECONDS);
          if (lastElement != null) {
            batch.add(lastElement);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }

        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
          exportCurrentBatch();
          updateNextExportTime();
        }
      }
    }

    private void flush() {
      int recordsToFlush = queue.size();
      while (recordsToFlush > 0) {
        LogData record = queue.poll();
        assert record != null;
        batch.add(record);
        recordsToFlush--;
        if (batch.size() >= maxExportBatchSize) {
          exportCurrentBatch();
        }
      }
      exportCurrentBatch();
      CompletableResultCode result = flushRequested.get();
      assert result != null;
      flushRequested.set(null);
    }

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    private void exportCurrentBatch() {
      if (batch.isEmpty()) {
        return;
      }

      try {
        final CompletableResultCode result = logExporter.export(batch);
        result.join(exporterTimeoutMillis, TimeUnit.MILLISECONDS);
        if (result.isSuccess()) {
          processedLogsCounter.add(batch.size(), successAttrs);
        } else {
          processedLogsCounter.add(1, failureAttrs);
        }
      } catch (RuntimeException t) {
        processedLogsCounter.add(batch.size(), failureAttrs);
      } finally {
        batch.clear();
      }
    }

    private CompletableResultCode shutdown() {
      final CompletableResultCode result = new CompletableResultCode();
      final CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(
          new Runnable() {
            @Override
            public void run() {
              continueWork = false;
              final CompletableResultCode shutdownResult = logExporter.shutdown();
              shutdownResult.whenComplete(
                  new Runnable() {
                    @Override
                    public void run() {
                      if (flushResult.isSuccess() && shutdownResult.isSuccess()) {
                        result.succeed();
                      } else {
                        result.fail();
                      }
                    }
                  });
            }
          });
      return result;
    }

    private CompletableResultCode forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      if (this.flushRequested.compareAndSet(null, flushResult)) {
        return flushResult;
      }
      flushResult = this.flushRequested.get();
      if (flushResult == null) {
        // A pending flush completed successfully while executing this method.
        return CompletableResultCode.ofSuccess();
      }
      return flushResult;
    }

    public void addLog(LogData logData) {
      if (!queue.offer(logData)) {
        processedLogsCounter.add(1, queueFullAttrs);
      }
    }
  }
}
