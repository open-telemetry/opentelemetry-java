/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.common.internal.ThrowableUtil;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.JcTools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

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

  private static final Logger logger = Logger.getLogger(BatchSpanProcessor.class.getName());

  private static final String WORKER_THREAD_NAME =
      BatchSpanProcessor.class.getSimpleName() + "_WorkerThread";

  private final boolean exportUnsampledSpans;
  private final Worker worker;

  /**
   * Returns a new {@link BatchSpanProcessor} with default configuration which batches spans
   * exported by the SDK then pushes them to the {@code spanExporter}.
   *
   * <p>This is a convenience method equivalent to {@code
   * BatchSpanProcessor.builder(spanExporter).build()}. Use {@link #builder(SpanExporter)} if you
   * need to customize the configuration.
   *
   * @param spanExporter the {@link SpanExporter} to which the Spans are pushed.
   * @return a new {@link BatchSpanProcessor}.
   * @since 1.64.0
   */
  public static BatchSpanProcessor create(SpanExporter spanExporter) {
    requireNonNull(spanExporter, "spanExporter");
    return builder(spanExporter).build();
  }

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
      Supplier<MeterProvider> meterProvider,
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
  // Package-private and extensible for deterministic worker lifecycle tests.
  static class Worker implements Runnable {

    private final SpanProcessorInstrumentation spanProcessorInstrumentation;

    private final SpanExporter spanExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<ReadableSpan> queue;
    private final AtomicInteger queueSize = new AtomicInteger();
    // While waiting, the worker publishes how many queued spans it needs to complete its batch.
    // Producers wake it once that threshold is reached. Integer.MAX_VALUE means it is not waiting.
    private final AtomicInteger spansNeeded = new AtomicInteger(Integer.MAX_VALUE);
    @Nullable private volatile Thread workerThread;
    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private volatile boolean continueWork = true;
    private final ArrayList<SpanData> batch;
    private final long maxQueueSize;
    private final AtomicInteger droppedSpanCount = new AtomicInteger(0);

    Worker(
        SpanExporter spanExporter,
        Supplier<MeterProvider> meterProvider,
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

      spanProcessorInstrumentation =
          SpanProcessorInstrumentation.get(
              telemetryVersion, ComponentId.generateLazy("batching_span_processor"), meterProvider);
      this.maxQueueSize = maxQueueSize;

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    void addSpan(ReadableSpan span) {
      if (isShutdown.get()) {
        spanProcessorInstrumentation.dropSpansAlreadyShutdown(1);
        return;
      }
      spanProcessorInstrumentation.buildQueueMetricsOnce(maxQueueSize, queue::size);
      if (!queue.offer(span)) {
        spanProcessorInstrumentation.dropSpansQueueFull(1);
        droppedSpanCount.incrementAndGet();
      } else if (queueSize.incrementAndGet() >= spansNeeded.get()) {
        LockSupport.unpark(workerThread);
      }
    }

    @Override
    public void run() {
      workerThread = Thread.currentThread();
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
        if (queue.isEmpty() && !awaitWork()) {
          return;
        }
      }
    }

    private boolean awaitWork() {
      int needed = maxExportBatchSize - batch.size();
      spansNeeded.set(needed);
      try {
        // Producers may have queued a complete batch before seeing the new threshold. A flush or
        // shutdown may also have been requested while the worker was finishing its previous loop.
        if (continueWork && flushRequested.get() == null && queueSize.get() < needed) {
          long remaining = nextExportTime - System.nanoTime();
          if (remaining > 0) {
            // Only the worker calls parkNanos. An earlier unpark leaves a permit, and spurious
            // wakeups are handled by rechecking work and the export deadline in the outer loop.
            LockSupport.parkNanos(this, remaining);
          }
        }
      } finally {
        spansNeeded.set(Integer.MAX_VALUE);
      }
      return !Thread.currentThread().isInterrupted();
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

    CompletableResultCode shutdown() {
      if (isShutdown.getAndSet(true)) {
        return CompletableResultCode.ofSuccess();
      }
      CompletableResultCode result = new CompletableResultCode();

      CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(() -> {
        continueWork = false;
        LockSupport.unpark(workerThread);
        CompletableResultCode shutdownResult = spanExporter.shutdown();
        shutdownResult.whenComplete(() -> {
          if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
            result.fail();
          } else {
            result.succeed();
          }
        });
      });

      return result;
    }

    CompletableResultCode forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      // we set the atomic here to trigger the worker loop to do a flush of the entire queue.
      if (flushRequested.compareAndSet(null, flushResult)) {
        LockSupport.unpark(workerThread);
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

      int dropped = droppedSpanCount.getAndSet(0);
      if (dropped > 0) {
        logger.log(
            Level.WARNING,
            "BatchSpanProcessor dropped "
                + dropped
                + " span(s) since the last export because the queue is full"
                + " (maxQueueSize="
                + maxQueueSize
                + ")");
      }

      try {
        // We always increment for every export invocation, so we increment before the export call
        // to make sure thrown errors don't affect it.
        spanProcessorInstrumentation.finishSpans(batch.size());
        CompletableResultCode result = spanExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (!result.isSuccess()) {
          logger.log(Level.FINE, "Exporter failed");
        }
      } catch (Throwable t) {
        ThrowableUtil.propagateIfFatal(t);
        logger.log(Level.WARNING, "Exporter threw an Exception", t);
      } finally {
        batch.clear();
      }
    }
  }
}
