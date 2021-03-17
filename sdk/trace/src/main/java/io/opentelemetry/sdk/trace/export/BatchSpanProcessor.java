/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the {@link SpanProcessor} that batches spans exported by the SDK then pushes
 * them to the exporter pipeline.
 *
 * <p>All spans reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full spans are dropped). Spans are exported either
 * when there are {@code maxExportBatchSize} pending spans or {@code scheduleDelayNanos} has passed
 * since the last export finished.
 *
 * <p>This batch {@link SpanProcessor} can cause high contention in a very high traffic service.
 * TODO: Add a link to the SpanProcessor that uses Disruptor as alternative with low contention.
 */
public final class BatchSpanProcessor implements SpanProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchSpanProcessor.class.getSimpleName() + "_WorkerThread";
  private static final String SPAN_PROCESSOR_TYPE_LABEL = "spanProcessorType";
  private static final String SPAN_PROCESSOR_TYPE_VALUE = BatchSpanProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchSpanProcessor}.
   *
   * @param spanExporter the {@code SpanExporter} to where the Spans are pushed.
   * @return a new {@link BatchSpanProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static BatchSpanProcessorBuilder builder(SpanExporter spanExporter) {
    return new BatchSpanProcessorBuilder(spanExporter);
  }

  BatchSpanProcessor(
      SpanExporter spanExporter,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.worker =
        new Worker(
            spanExporter,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize));
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
    if (!span.getSpanContext().isSampled()) {
      return;
    }
    worker.addSpan(span);
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

  // Visible for testing
  ArrayList<SpanData> getBatch() {
    return worker.batch;
  }

  // Worker is a thread that batches multiple spans and calls the registered SpanExporter to export
  // the data.
  private static final class Worker extends WorkerBase {

    private long nextExportTime;

    private final ArrayList<SpanData> batch;

    private Worker(
        SpanExporter spanExporter,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        BlockingQueue<ReadableSpan> queue) {
      super(
          spanExporter,
          scheduleDelayNanos,
          maxExportBatchSize,
          exporterTimeoutNanos,
          queue,
          SPAN_PROCESSOR_TYPE_LABEL,
          SPAN_PROCESSOR_TYPE_VALUE);

      this.batch = new ArrayList<>(maxExportBatchSize);
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork.get()) {
        if (flushRequested.get() != null) {
          flush();
        }

        try {
          ReadableSpan lastElement = queue.poll(100, TimeUnit.MILLISECONDS);
          if (lastElement != null) {
            batch.add(lastElement.toSpanData());
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

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    @Override
    protected Collection<SpanData> getBatch() {
      return batch;
    }
  }
}
