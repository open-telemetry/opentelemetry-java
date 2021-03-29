/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("FutureReturnValueIgnored")
public final class ExecutorServiceSpanProcessor implements SpanProcessor {

  private static final String SPAN_PROCESSOR_TYPE_LABEL = "spanProcessorType";
  private static final String SPAN_PROCESSOR_TYPE_VALUE =
      ExecutorServiceSpanProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final boolean ownsExecutorService;
  private final ScheduledExecutorService executorService;

  public static ExecutorServiceSpanProcessorBuilder builder(
      SpanExporter spanExporter,
      ScheduledExecutorService executorService,
      boolean ownsExecutorService) {
    return new ExecutorServiceSpanProcessorBuilder(
        spanExporter, executorService, ownsExecutorService);
  }

  ExecutorServiceSpanProcessor(
      SpanExporter spanExporter,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos,
      ScheduledExecutorService executorService,
      boolean ownsExecutorService,
      long workerScheduleIntervalNanos) {
    this.worker =
        new Worker(
            spanExporter,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize),
            SPAN_PROCESSOR_TYPE_LABEL,
            SPAN_PROCESSOR_TYPE_VALUE,
            executorService,
            isShutdown,
            workerScheduleIntervalNanos);
    this.ownsExecutorService = ownsExecutorService;
    this.executorService = executorService;
    executorService.schedule(worker, workerScheduleIntervalNanos, TimeUnit.NANOSECONDS);
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
    CompletableResultCode result = worker.shutdown();
    // do the cleanup after worker finishes flush
    result.whenComplete(
        () -> {
          if (ownsExecutorService) {
            executorService.shutdown();
          }
        });

    return result;
  }

  @Override
  public CompletableResultCode forceFlush() {
    return worker.forceFlush();
  }

  // Visible for testing
  List<SpanData> getBatch() {
    return new ArrayList<>(worker.getBatch());
  }

  private static class Worker extends WorkerBase {

    private final AtomicLong nextExportTime = new AtomicLong();

    private final ArrayBlockingQueue<SpanData> batch;

    private final AtomicBoolean isShutdown;

    private final long workerScheduleIntervalNanos;

    private Worker(
        SpanExporter spanExporter,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        BlockingQueue<ReadableSpan> queue,
        String spanProcessorTypeLabel,
        String spanProcessorTypeValue,
        ScheduledExecutorService executorService,
        AtomicBoolean isShutdown,
        long workerScheduleIntervalNanos) {
      super(
          spanExporter,
          scheduleDelayNanos,
          maxExportBatchSize,
          exporterTimeoutNanos,
          queue,
          spanProcessorTypeLabel,
          spanProcessorTypeValue,
          executorService);

      this.isShutdown = isShutdown;
      this.batch = new ArrayBlockingQueue<>(maxExportBatchSize);
      this.workerScheduleIntervalNanos = workerScheduleIntervalNanos;
      updateNextExportTime();
    }

    private void updateNextExportTime() {
      nextExportTime.set(System.nanoTime() + scheduleDelayNanos);
    }

    @Override
    public void run() {
      // nextExportTime is set for the first time in the constructor

      continueWork.set(true);
      while (continueWork.get() && !isShutdown.get()) {
        if (flushRequested.get() != null) {
          flush();
        }

        try {
          ReadableSpan lastElement = queue.peek();
          if (lastElement != null) {
            batch.add(lastElement.toSpanData());
            // drain queue
            queue.take();
          } else {
            // nothing in the queue, so schedule next run and release the thread
            continueWork.set(false);
            scheduleNextRun();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }

        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime.get()) {
          continueWork.set(false);
          exportCurrentBatch();
          updateNextExportTime();
        }
      }
      // flush may be requested when processor shuts down
      if (flushRequested.get() != null) {
        flush();
      }
    }

    @Override
    public Collection<SpanData> getBatch() {
      return batch;
    }

    @Override
    protected void scheduleNextRun() {
      if (!isShutdown.get()) {
        executorService.schedule(this, workerScheduleIntervalNanos, TimeUnit.NANOSECONDS);
      }
    }
  }
}
