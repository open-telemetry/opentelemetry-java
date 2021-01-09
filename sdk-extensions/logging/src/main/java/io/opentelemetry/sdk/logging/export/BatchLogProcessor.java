/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.export;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.logging.LogProcessor;
import io.opentelemetry.sdk.logging.data.LogRecord;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class BatchLogProcessor implements LogProcessor {
  private static final String WORKER_THREAD_NAME =
      BatchLogProcessor.class.getSimpleName() + "_WorkerThread";

  private final Worker worker;
  private final Thread workerThread;

  private BatchLogProcessor(
      int maxQueueSize,
      long scheduleDelayMillis,
      int maxExportBatchSize,
      long exporterTimeoutMillis,
      LogExporter logExporter) {
    this.worker =
        new Worker(
            logExporter,
            scheduleDelayMillis,
            maxExportBatchSize,
            exporterTimeoutMillis,
            new ArrayBlockingQueue<LogRecord>(maxQueueSize));
    this.workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    this.workerThread.start();
  }

  public static Builder builder(LogExporter logExporter) {
    return new Builder(logExporter);
  }

  @Override
  public void addLogRecord(LogRecord record) {
    worker.addLogRecord(record);
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
    static {
      Meter meter = GlobalMetricsProvider.getMeter("io.opentelemetry.sdk.logging");
      LongCounter logRecordsProcessed =
          meter
              .longCounterBuilder("logRecordsProcessed")
              .setUnit("1")
              .setDescription("Number of records processed")
              .build();
      successCounter = logRecordsProcessed.bind(Labels.of("result", "success"));
      exporterFailureCounter =
          logRecordsProcessed.bind(
              Labels.of("result", "dropped record", "cause", "exporter failure"));
      queueFullRecordCounter =
          logRecordsProcessed.bind(Labels.of("result", "dropped record", "cause", "queue full"));
    }

    private static final BoundLongCounter exporterFailureCounter;
    private static final BoundLongCounter queueFullRecordCounter;
    private static final BoundLongCounter successCounter;

    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final LogExporter logExporter;
    private final long exporterTimeoutMillis;
    private final ArrayList<LogRecord> batch;
    private final BlockingQueue<LogRecord> queue;

    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private long nextExportTime;

    private Worker(
        LogExporter logExporter,
        long scheduleDelayMillis,
        int maxExportBatchSize,
        long exporterTimeoutMillis,
        BlockingQueue<LogRecord> queue) {
      this.logExporter = logExporter;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      this.scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(scheduleDelayMillis);
      this.queue = queue;
      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        if (flushRequested.get() != null) {
          flush();
        }

        try {
          LogRecord lastElement = queue.poll(100, TimeUnit.MILLISECONDS);
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
        LogRecord record = queue.poll();
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
          successCounter.add(batch.size());
        } else {
          exporterFailureCounter.add(1);
        }
      } catch (Exception t) {
        exporterFailureCounter.add(batch.size());
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
      this.flushRequested.compareAndSet(null, flushResult);
      return this.flushRequested.get();
    }

    public void addLogRecord(LogRecord record) {
      if (!queue.offer(record)) {
        queueFullRecordCounter.add(1);
      }
    }
  }

  @SuppressWarnings("deprecation") // Remove after ConfigBuilder is deleted
  public static class Builder extends io.opentelemetry.sdk.common.export.ConfigBuilder<Builder> {
    private static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 200;
    private static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
    private static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
    private static final long DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;

    private static final String KEY_SCHEDULE_DELAY_MILLIS = "otel.log.schedule.delay";
    private static final String KEY_MAX_QUEUE_SIZE = "otel.log.max.queue";
    private static final String KEY_MAX_EXPORT_BATCH_SIZE = "otel.log.max.export.batch";
    private static final String KEY_EXPORT_TIMEOUT_MILLIS = "otel.log.export.timeout";

    private final LogExporter logExporter;
    private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
    private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
    private long exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;

    private Builder(LogExporter logExporter) {
      this.logExporter = Objects.requireNonNull(logExporter, "Exporter argument can not be null");
    }

    /**
     * Build a BatchLogProcessor.
     *
     * @return configured processor
     */
    public BatchLogProcessor build() {
      return new BatchLogProcessor(
          maxQueueSize,
          scheduleDelayMillis,
          maxExportBatchSize,
          exporterTimeoutMillis,
          logExporter);
    }

    /**
     * Sets the delay interval between two consecutive exports. The actual interval may be shorter
     * if the batch size is getting larger than {@code maxQueuedSpans / 2}.
     *
     * <p>Default value is {@code 250}ms.
     *
     * @param scheduleDelayMillis the delay interval between two consecutive exports.
     * @return this.
     * @see BatchLogProcessor.Builder#DEFAULT_SCHEDULE_DELAY_MILLIS
     */
    public BatchLogProcessor.Builder setScheduleDelayMillis(long scheduleDelayMillis) {
      this.scheduleDelayMillis = scheduleDelayMillis;
      return this;
    }

    public long getScheduleDelayMillis() {
      return scheduleDelayMillis;
    }

    /**
     * Sets the maximum time an exporter will be allowed to run before being cancelled.
     *
     * <p>Default value is {@code 30000}ms
     *
     * @param exporterTimeoutMillis the timeout for exports in milliseconds.
     * @return this
     * @see BatchLogProcessor.Builder#DEFAULT_EXPORT_TIMEOUT_MILLIS
     */
    public Builder setExporterTimeoutMillis(int exporterTimeoutMillis) {
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      return this;
    }

    public long getExporterTimeoutMillis() {
      return exporterTimeoutMillis;
    }

    /**
     * Sets the maximum number of Spans that are kept in the queue before start dropping.
     *
     * <p>See the BatchSampledSpansProcessor class description for a high-level design description
     * of this class.
     *
     * <p>Default value is {@code 2048}.
     *
     * @param maxQueueSize the maximum number of Spans that are kept in the queue before start
     *     dropping.
     * @return this.
     * @see BatchLogProcessor.Builder#DEFAULT_MAX_QUEUE_SIZE
     */
    public Builder setMaxQueueSize(int maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
      return this;
    }

    public int getMaxQueueSize() {
      return maxQueueSize;
    }

    /**
     * Sets the maximum batch size for every export. This must be smaller or equal to {@code
     * maxQueuedSpans}.
     *
     * <p>Default value is {@code 512}.
     *
     * @param maxExportBatchSize the maximum batch size for every export.
     * @return this.
     * @see BatchLogProcessor.Builder#DEFAULT_MAX_EXPORT_BATCH_SIZE
     */
    public Builder setMaxExportBatchSize(int maxExportBatchSize) {
      Utils.checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
      this.maxExportBatchSize = maxExportBatchSize;
      return this;
    }

    public int getMaxExportBatchSize() {
      return maxExportBatchSize;
    }

    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Long longValue = getLongProperty(KEY_SCHEDULE_DELAY_MILLIS, configMap);
      if (longValue != null) {
        this.setScheduleDelayMillis(longValue);
      }
      Integer intValue = getIntProperty(KEY_MAX_QUEUE_SIZE, configMap);
      if (intValue != null) {
        this.setMaxQueueSize(intValue);
      }
      intValue = getIntProperty(KEY_MAX_EXPORT_BATCH_SIZE, configMap);
      if (intValue != null) {
        this.setMaxExportBatchSize(intValue);
      }
      intValue = getIntProperty(KEY_EXPORT_TIMEOUT_MILLIS, configMap);
      if (intValue != null) {
        this.setExporterTimeoutMillis(intValue);
      }
      return this;
    }
  }
}
