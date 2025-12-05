/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.internal.ThrowableUtil;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.JcTools;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link SpanProcessor} that batches spans exported by the SDK then pushes
 * them to the exporter pipeline.
 *
 * <p>All spans reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full spans are dropped). Spans are exported either
 * when there are {@code maxExportBatchSize} pending spans or {@code scheduleDelayNanos} has passed
 * since the last export finished.
 */
public final class BatchSpanProcessor implements SpanProcessor {

  private static final ComponentId COMPONENT_ID =
      ComponentId.generateLazy("batching_span_processor");

  private static final Logger logger = Logger.getLogger(BatchSpanProcessor.class.getName());

  private static final String WORKER_THREAD_NAME =
      BatchSpanProcessor.class.getSimpleName() + "_WorkerThread";

  private final boolean exportUnsampledSpans;
  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchSpanProcessor}.
   *
   * @param spanExporter the {@link SpanExporter} to which the Spans are pushed.
   * @return a new {@link BatchSpanProcessorBuilder}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static BatchSpanProcessorBuilder builder(SpanExporter spanExporter) {
    return new BatchSpanProcessorBuilder(spanExporter);
  }

  BatchSpanProcessor(
      SpanExporter spanExporter,
      boolean exportUnsampledSpans,
      MeterProvider meterProvider,
      InternalTelemetryVersion telemetryVersion,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.exportUnsampledSpans = exportUnsampledSpans;
    this.worker =
        new Worker(
            spanExporter,
            meterProvider,
            telemetryVersion,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            JcTools.newFixedSizeQueue(maxQueueSize),
            maxQueueSize);
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    if (span != null && (exportUnsampledSpans || span.getSpanContext().isSampled())) {
      worker.addSpan(span);
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
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
   * Return the processor's configured {@link SpanExporter}.
   *
   * @since 1.37.0
   */
  public SpanExporter getSpanExporter() {
    return worker.spanExporter;
  }

  // Visible for testing
  List<SpanData> getBatch() {
    return worker.batch;
  }

  // Visible for testing
  Queue<ReadableSpan> getQueue() {
    return worker.queue;
  }

  @Override
  public String toString() {
    return "BatchSpanProcessor{"
        + "spanExporter="
        + worker.spanExporter
        + ", exportUnsampledSpans="
        + exportUnsampledSpans
        + ", scheduleDelayNanos="
        + worker.scheduleDelayNanos
        + ", maxExportBatchSize="
        + worker.maxExportBatchSize
        + ", exporterTimeoutNanos="
        + worker.exporterTimeoutNanos
        + '}';
  }

  // Worker is a thread that batches multiple spans and calls the registered SpanExporter to export
  // the data.
  private static final class Worker implements Runnable {

    private final SpanProcessorMetrics spanProcessorMetrics;

    private final SpanExporter spanExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<ReadableSpan> queue;
    private final AtomicInteger queueSize = new AtomicInteger();
    // When waiting on the spans queue, exporter thread sets this atomic to the number of more
    // spans it needs before doing an export. Writer threads would then wait for the queue to reach
    // spansNeeded size before notifying the exporter thread about new entries.
    // Integer.MAX_VALUE is used to imply that exporter thread is not expecting any signal. Since
    // exporter thread doesn't expect any signal initially, this value is initialized to
    // Integer.MAX_VALUE.
    private final AtomicInteger spansNeeded = new AtomicInteger(Integer.MAX_VALUE);
    private final BlockingQueue<Boolean> signal;
    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private final ArrayList<SpanData> batch;

    private Worker(
        SpanExporter spanExporter,
        MeterProvider meterProvider,
        InternalTelemetryVersion telemetryVersion,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        Queue<ReadableSpan> queue,
        long maxQueueSize) {
      this.spanExporter = spanExporter;
      this.scheduleDelayNanos = scheduleDelayNanos;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutNanos = exporterTimeoutNanos;
      this.queue = queue;
      this.signal = new ArrayBlockingQueue<>(1);

      spanProcessorMetrics =
          SpanProcessorMetrics.get(telemetryVersion, COMPONENT_ID, meterProvider);
      spanProcessorMetrics.buildQueueCapacityMetric(maxQueueSize);
      spanProcessorMetrics.buildQueueSizeMetric(queue::size);

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addSpan(ReadableSpan span) {
      if (!queue.offer(span)) {
        spanProcessorMetrics.dropSpans(1);
      } else {
        if (queueSize.incrementAndGet() >= spansNeeded.get()) {
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
        drain(maxExportBatchSize - batch.size());

        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
          exportCurrentBatch();
          updateNextExportTime();
        }
        if (queue.isEmpty()) {
          try {
            long pollWaitTime = nextExportTime - System.nanoTime();
            if (pollWaitTime > 0) {
              spansNeeded.set(maxExportBatchSize - batch.size());
              signal.poll(pollWaitTime, TimeUnit.NANOSECONDS);
              spansNeeded.set(Integer.MAX_VALUE);
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }

    private int drain(int limit) {
      int drained = JcTools.drain(queue, limit, span -> batch.add(span.toSpanData()));
      queueSize.addAndGet(-drained);
      return drained;
    }

    private void flush() {
      int spansToFlush = queueSize.get();
      while (spansToFlush > 0) {
        int drained = drain(maxExportBatchSize - batch.size());
        spansToFlush -= drained;
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
            CompletableResultCode shutdownResult = spanExporter.shutdown();
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
        CompletableResultCode result = spanExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (!result.isSuccess()) {
          logger.log(Level.FINE, "Exporter failed");
          error = "export_failed";
        }
      } catch (Throwable t) {
        ThrowableUtil.propagateIfFatal(t);
        logger.log(Level.WARNING, "Exporter threw an Exception", t);
        error = t.getClass().getName();
      } finally {
        spanProcessorMetrics.finishSpans(batch.size(), error);
        batch.clear();
      }
    }
  }
}
