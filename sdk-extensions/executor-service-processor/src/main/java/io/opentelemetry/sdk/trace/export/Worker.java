/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Worker extends WorkerBase {

  private final AtomicLong nextExportTime = new AtomicLong();

  private final ArrayBlockingQueue<SpanData> batch;

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
  public Worker(
      SpanExporter spanExporter,
      long scheduleDelayNanos,
      int maxExportBatchSize,
      long exporterTimeoutNanos,
      BlockingQueue<ReadableSpan> queue,
      String spanProcessorTypeLabel,
      String spanProcessorTypeValue) {
    super(
        spanExporter,
        scheduleDelayNanos,
        maxExportBatchSize,
        exporterTimeoutNanos,
        queue,
        spanProcessorTypeLabel,
        spanProcessorTypeValue);

    this.batch = new ArrayBlockingQueue<>(maxExportBatchSize);
    updateNextExportTime();
  }

  private void updateNextExportTime() {
    nextExportTime.set(System.nanoTime() + scheduleDelayNanos);
  }

  @Override
  public void run() {
    // nextExportTime is set for the first time in the constructor

    continueWork.set(true);
    while (continueWork.get()) {
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
          continueWork.set(false);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime.get()) {
        exportCurrentBatch();
        updateNextExportTime();
      }
    }
  }

  @Override
  public Collection<SpanData> getBatch() {
    return batch;
  }
}
