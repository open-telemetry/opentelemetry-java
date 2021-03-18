/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.export;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.export.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorServiceSpanProcessor implements SpanProcessor {

  private static final String SPAN_PROCESSOR_TYPE_LABEL = "spanProcessorType";
  private static final String SPAN_PROCESSOR_TYPE_VALUE =
      ExecutorServiceSpanProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final boolean ownsExecutorService;
  private final ScheduledExecutorService executorService;
  private final ScheduledFuture<?> future;

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
      long workerScheduleInterval) {
    this.worker =
        new Worker(
            spanExporter,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize),
            SPAN_PROCESSOR_TYPE_LABEL,
            SPAN_PROCESSOR_TYPE_VALUE);
    this.ownsExecutorService = ownsExecutorService;
    this.executorService = executorService;
    this.future =
        executorService.scheduleWithFixedDelay(
            worker, workerScheduleInterval, workerScheduleInterval, TimeUnit.MILLISECONDS);
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
          future.cancel(false);
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
}
