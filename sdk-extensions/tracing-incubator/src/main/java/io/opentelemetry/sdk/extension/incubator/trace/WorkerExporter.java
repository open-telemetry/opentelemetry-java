/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

class WorkerExporter {

  private final SpanExporter spanExporter;
  private final ScheduledExecutorService executorService;
  private final Logger logger;
  private final long exporterTimeoutNanos;
  private final BoundLongCounter exportedSpanCounter;
  private final AtomicReference<CompletableResultCode> flushSignal;
  private final int maxExportBatchSize;

  WorkerExporter(
      SpanExporter spanExporter,
      ScheduledExecutorService executorService,
      Logger logger,
      long exporterTimeoutNanos,
      BoundLongCounter exportedSpanCounter,
      AtomicReference<CompletableResultCode> flushSignal,
      int maxExportBatchSize) {
    this.spanExporter = spanExporter;
    this.executorService = executorService;
    this.logger = logger;
    this.exporterTimeoutNanos = exporterTimeoutNanos;
    this.exportedSpanCounter = exportedSpanCounter;
    this.flushSignal = flushSignal;
    this.maxExportBatchSize = maxExportBatchSize;
  }

  /**
   * Exports spans in current batch.
   *
   * @param batch Collection containing {@link SpanData} to export
   * @return a {@link CompletableResultCode} which completes when export completes or timeouts
   */
  public CompletableResultCode exportCurrentBatch(Collection<SpanData> batch) {
    final CompletableResultCode thisOpResult = new CompletableResultCode();
    if (batch.isEmpty()) {
      thisOpResult.succeed();
      return thisOpResult;
    }

    final CompletableResultCode result = spanExporter.export(new ArrayList<>(batch));
    final AtomicBoolean cleaner = new AtomicBoolean(true);
    final ScheduledFuture<?> timeoutHandler =
        executorService.schedule(
            () -> {
              if (cleaner.compareAndSet(true, false)) {
                logger.log(Level.FINE, "Timeout happened when waiting for export to complete");
                batch.clear();
                thisOpResult.fail();
              }
            },
            exporterTimeoutNanos,
            TimeUnit.NANOSECONDS);

    result.whenComplete(
        () -> {
          if (cleaner.compareAndSet(true, false)) {
            timeoutHandler.cancel(true);
            if (result.isSuccess()) {
              exportedSpanCounter.add(batch.size());
              batch.clear();
              thisOpResult.succeed();
            } else {
              logger.log(Level.FINE, "Exporter failed");
              batch.clear();
              thisOpResult.fail();
            }
          }
        });
    return thisOpResult;
  }

  /**
   * Flushes (exports) spans from both batch and queue.
   *
   * @param batch a collection of {@link SpanData} to export
   * @param queue {@link ReadableSpan} queue to be drained and then exported
   */
  public void flush(Collection<SpanData> batch, AbstractQueue<ReadableSpan> queue) {
    int spansToFlush = queue.size();
    while (spansToFlush > 0) {
      ReadableSpan span = queue.poll();
      assert span != null;
      batch.add(span.toSpanData());
      spansToFlush--;
      if (batch.size() >= maxExportBatchSize) {
        exportCurrentBatch(batch);
      }
    }
    exportCurrentBatch(batch);
    flushSignal.get().succeed();
    flushSignal.set(null);
  }
}
