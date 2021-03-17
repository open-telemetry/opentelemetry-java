/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WorkerBase implements Runnable {

  private final BoundLongCounter droppedSpans;
  protected final BoundLongCounter exportedSpans;

  protected final Logger logger = Logger.getLogger(getClass().getName());
  protected final SpanExporter spanExporter;
  protected final long scheduleDelayNanos;
  protected final int maxExportBatchSize;
  protected final long exporterTimeoutNanos;

  protected final BlockingQueue<ReadableSpan> queue;

  protected final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
  protected final AtomicBoolean continueWork = new AtomicBoolean(true);

  /**
   * This a base class useful for all flavors of BatchSpanProcessor.
   *
   * @param spanExporter {@link SpanExporter} to use
   * @param scheduleDelayNanos how much time can pass before Worker sends a batch to exporter
   * @param maxExportBatchSize max size of a batch
   * @param exporterTimeoutNanos {@link SpanExporter} timeout
   * @param queue thread-safe queue for processing spans
   * @param spanProcessorTypeLabel (ie. queueSize or processedSpans) metric label
   * @param spanProcessorTypeValue (ie. queueSize or processedSpans) metric value
   */
  protected WorkerBase(
      SpanExporter spanExporter,
      long scheduleDelayNanos,
      int maxExportBatchSize,
      long exporterTimeoutNanos,
      BlockingQueue<ReadableSpan> queue,
      String spanProcessorTypeLabel,
      String spanProcessorTypeValue) {
    this.spanExporter = spanExporter;
    this.scheduleDelayNanos = scheduleDelayNanos;
    this.maxExportBatchSize = maxExportBatchSize;
    this.exporterTimeoutNanos = exporterTimeoutNanos;
    this.queue = queue;
    Meter meter = GlobalMetricsProvider.getMeter("io.opentelemetry.sdk.trace");
    meter
        .longValueObserverBuilder("queueSize")
        .setDescription("The number of spans queued")
        .setUnit("1")
        .setUpdater(
            result ->
                result.observe(
                    queue.size(), Labels.of(spanProcessorTypeLabel, spanProcessorTypeValue)))
        .build();
    LongCounter processedSpansCounter =
        meter
            .longCounterBuilder("processedSpans")
            .setUnit("1")
            .setDescription(
                "The number of spans processed by the BatchSpanProcessor. "
                    + "[dropped=true if they were dropped due to high throughput]")
            .build();
    droppedSpans =
        processedSpansCounter.bind(
            Labels.of(spanProcessorTypeLabel, spanProcessorTypeValue, "dropped", "true"));
    exportedSpans =
        processedSpansCounter.bind(
            Labels.of(spanProcessorTypeLabel, spanProcessorTypeValue, "dropped", "false"));
  }

  /**
   * Adds a span to worker's internal queue.
   *
   * @param span {@link ReadableSpan}
   */
  public void addSpan(ReadableSpan span) {
    if (!queue.offer(span)) {
      droppedSpans.add(1);
    }
  }

  protected abstract Collection<SpanData> getBatch();

  protected void exportCurrentBatch() {
    Collection<SpanData> batch = getBatch();
    if (batch.isEmpty()) {
      return;
    }

    try {
      final CompletableResultCode result = spanExporter.export(new ArrayList<>(batch));
      result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
      if (result.isSuccess()) {
        exportedSpans.add(batch.size());
      } else {
        logger.log(Level.FINE, "Exporter failed");
      }
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Exporter threw an Exception", e);
    } finally {
      batch.clear();
    }
  }

  /**
   * Processes all span events that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which completes when currently queued spans are
   *     finished processing.
   */
  public CompletableResultCode forceFlush() {
    logger.info("Force flush");
    CompletableResultCode flushResult = new CompletableResultCode();
    // we set the atomic here to trigger the worker loop to do a flush on its next iteration.
    flushRequested.compareAndSet(null, flushResult);
    CompletableResultCode possibleResult = flushRequested.get();
    // there's a race here where the flush happening in the worker loop could complete before we
    // get what's in the atomic. In that case, just return success, since we know it succeeded in
    // the interim.
    return possibleResult == null ? CompletableResultCode.ofSuccess() : possibleResult;
  }

  /**
   * Processes all span events that have not yet been processed and closes used resources.
   *
   * @return a {@link CompletableResultCode} which completes when shutdown is finished.
   */
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();

    final CompletableResultCode flushResult = forceFlush();
    flushResult.whenComplete(
        () -> {
          continueWork.set(false);
          final CompletableResultCode shutdownResult = spanExporter.shutdown();
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

  protected void flush() {
    Collection<SpanData> batch = getBatch();
    int spansToFlush = queue.size();
    while (spansToFlush > 0) {
      ReadableSpan span = queue.poll();
      assert span != null;
      batch.add(span.toSpanData());
      spansToFlush--;
      if (batch.size() >= maxExportBatchSize) {
        exportCurrentBatch();
      }
    }
    exportCurrentBatch();
    flushRequested.get().succeed();
    flushRequested.set(null);
  }
}
