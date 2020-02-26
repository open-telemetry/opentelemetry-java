/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.export;

import com.google.common.util.concurrent.MoreExecutors;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

/**
 * Implementation of the {@link SpanProcessor} that batches spans exported by the SDK then pushes
 * them to the exporter pipeline.
 *
 * <p>All spans reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, after the size is reached spans are dropped) and exported
 * every {@code scheduleDelayMillis} to the exporter pipeline in batches of {@code
 * maxExportBatchSize}.
 *
 * <p>If the queue gets half full a preemptive notification is sent to the worker thread that
 * exports the spans to wake up and start a new export cycle.
 *
 * <p>This batch {@link SpanProcessor} can cause high contention in a very high traffic service.
 * TODO: Add a link to the SpanProcessor that uses Disruptor as alternative with low contention.
 */
public final class BatchSpansProcessor implements SpanProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchSpansProcessor.class.getSimpleName() + "_WorkerThread";
  private static final String EXPORTER_THREAD_NAME =
      BatchSpansProcessor.class.getSimpleName() + "_ExporterThread";
  private final Worker worker;
  private final Thread workerThread;
  private final boolean sampled;

  private BatchSpansProcessor(
      SpanExporter spanExporter,
      boolean sampled,
      long scheduleDelayMillis,
      int maxQueueSize,
      int maxExportBatchSize,
      int exporterTimeoutMillis) {
    this.worker =
        new Worker(
            spanExporter,
            scheduleDelayMillis,
            maxQueueSize,
            maxExportBatchSize,
            exporterTimeoutMillis);
    this.workerThread = newThread(worker);
    this.workerThread.start();
    this.sampled = sampled;
  }

  @Override
  public void onStart(ReadableSpan span) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    if (sampled && !span.getSpanContext().getTraceFlags().isSampled()) {
      return;
    }
    worker.addSpan(span);
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public void shutdown() {
    workerThread.interrupt();
    worker.shutdown();
  }

  @Override
  public void forceFlush() {
    worker.forceFlush();
  }

  /**
   * Returns a new Builder for {@link BatchSpansProcessor}.
   *
   * @param spanExporter the {@code SpanExporter} to where the Spans are pushed.
   * @return a new {@link BatchSpansProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static Builder newBuilder(SpanExporter spanExporter) {
    return new Builder(spanExporter);
  }

  /** Builder class for {@link BatchSpansProcessor}. */
  public static final class Builder {

    private static final long SCHEDULE_DELAY_MILLIS = 5000;
    private static final int MAX_QUEUE_SIZE = 2048;
    private static final int MAX_EXPORT_BATCH_SIZE = 512;
    private static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;

    private final SpanExporter spanExporter;
    private long scheduleDelayMillis = SCHEDULE_DELAY_MILLIS;
    private int maxQueueSize = MAX_QUEUE_SIZE;
    private int maxExportBatchSize = MAX_EXPORT_BATCH_SIZE;
    private int exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;
    private boolean sampled = true;

    private Builder(SpanExporter spanExporter) {
      this.spanExporter = Utils.checkNotNull(spanExporter, "spanExporter");
    }

    // TODO: Consider to add support for constant Attributes and/or Resource.

    /**
     * Set whether only sampled spans should be reported.
     *
     * @param sampled report only sampled spans.
     * @return this.
     */
    public Builder reportOnlySampled(boolean sampled) {
      this.sampled = sampled;
      return this;
    }

    /**
     * Sets the delay interval between two consecutive exports. The actual interval may be shorter
     * if the batch size is getting larger than {@code maxQueuedSpans / 2}.
     *
     * <p>Default value is {@code 5000}ms.
     *
     * @param scheduleDelayMillis the delay interval between two consecutive exports.
     * @return this.
     */
    public Builder setScheduleDelayMillis(long scheduleDelayMillis) {
      this.scheduleDelayMillis = scheduleDelayMillis;
      return this;
    }

    /**
     * Sets the maximum time an exporter will be allowed to run before being cancelled.
     *
     * <p>Default value is {@code 30000}ms
     *
     * @param exporterTimeoutMillis the timeout for exports in milliseconds.
     * @return this
     */
    public Builder setExporterTimeoutMillis(int exporterTimeoutMillis) {
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      return this;
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
     */
    public Builder setMaxQueueSize(int maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
      return this;
    }

    /**
     * Sets the maximum batch size for every export. This must be smaller or equal to {@code
     * maxQueuedSpans}.
     *
     * <p>Default value is {@code 512}.
     *
     * @param maxExportBatchSize the maximum batch size for every export.
     * @return this.
     */
    public Builder setMaxExportBatchSize(int maxExportBatchSize) {
      Utils.checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
      this.maxExportBatchSize = maxExportBatchSize;
      return this;
    }

    /**
     * Returns a new {@link BatchSpansProcessor} that batches, then converts spans to proto and
     * forwards them to the given {@code spanExporter}.
     *
     * @return a new {@link BatchSpansProcessor}.
     * @throws NullPointerException if the {@code spanExporter} is {@code null}.
     */
    public BatchSpansProcessor build() {
      return new BatchSpansProcessor(
          spanExporter,
          sampled,
          scheduleDelayMillis,
          maxQueueSize,
          maxExportBatchSize,
          exporterTimeoutMillis);
    }
  }

  private static Thread newThread(Runnable runnable) {
    Thread thread = MoreExecutors.platformThreadFactory().newThread(runnable);
    try {
      thread.setName(WORKER_THREAD_NAME);
    } catch (SecurityException e) {
      // OK if we can't set the name in this environment.
    }
    return thread;
  }

  // Worker is a thread that batches multiple spans and calls the registered SpanExporter to export
  // the data.
  //
  // The list of batched data is protected by an explicit monitor object which ensures full
  // concurrency.
  private static final class Worker implements Runnable {

    static {
      Meter meter = OpenTelemetry.getMeterRegistry().get("io.opentelemetry.sdk.trace");
      LongCounter droppedSpansCounter =
          meter
              .longCounterBuilder("droppedSpans")
              .setMonotonic(true)
              .setUnit("1")
              .setDescription(
                  "The number of spans dropped by the BatchSpansProcessor due to high throughput.")
              .build();
      droppedSpans =
          droppedSpansCounter.bind(
              meter.createLabelSet("spanProcessorType", BatchSpansProcessor.class.getSimpleName()));
    }

    private static final BoundLongCounter droppedSpans;

    private final ExecutorService executorService =
        Executors.newSingleThreadExecutor(
            new ThreadFactory() {
              @Override
              public Thread newThread(Runnable r) {
                return new Thread(r, EXPORTER_THREAD_NAME);
              }
            });

    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private final SpanExporter spanExporter;
    private final long scheduleDelayMillis;
    private final int maxQueueSize;
    private final int maxExportBatchSize;
    private final int halfMaxQueueSize;
    private final Object monitor = new Object();
    private final int exporterTimeoutMillis;

    @GuardedBy("monitor")
    private final List<ReadableSpan> spansList;

    private Worker(
        SpanExporter spanExporter,
        long scheduleDelayMillis,
        int maxQueueSize,
        int maxExportBatchSize,
        int exporterTimeoutMillis) {
      this.spanExporter = spanExporter;
      this.scheduleDelayMillis = scheduleDelayMillis;
      this.maxQueueSize = maxQueueSize;
      this.halfMaxQueueSize = maxQueueSize >> 1;
      this.maxExportBatchSize = maxExportBatchSize;
      this.spansList = new ArrayList<>(maxQueueSize);
      this.exporterTimeoutMillis = exporterTimeoutMillis;
    }

    private void addSpan(ReadableSpan span) {
      synchronized (monitor) {
        if (spansList.size() == maxQueueSize) {
          droppedSpans.add(1);
          return;
        }
        // TODO: Record a gauge for referenced spans.
        spansList.add(span);
        // Notify the worker thread that at half of the queue is available. It will take
        // time anyway for the thread to wake up.
        if (spansList.size() >= halfMaxQueueSize) {
          monitor.notifyAll();
        }
      }
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        // Copy all the batched spans in a separate list to release the monitor lock asap to
        // avoid blocking the producer thread.
        ArrayList<ReadableSpan> spansCopy;
        synchronized (monitor) {
          // If still maxExportBatchSize elements in the queue better to execute an extra
          if (spansList.size() < maxExportBatchSize) {
            do {
              // In the case of a spurious wakeup we export only if we have at least one span in
              // the batch. It is acceptable because batching is a best effort mechanism here.
              try {
                monitor.wait(scheduleDelayMillis);
              } catch (InterruptedException ie) {
                // Preserve the interruption status as per guidance and stop doing any work.
                Thread.currentThread().interrupt();
                return;
              }
            } while (spansList.isEmpty());
          }
          spansCopy = new ArrayList<>(spansList);
          spansList.clear();
        }
        // Execute the batch export outside the synchronized to not block all producers.
        exportBatches(spansCopy);
      }
    }

    private void shutdown() {
      forceFlush();
      executorService.shutdown();
    }

    private void forceFlush() {
      ArrayList<ReadableSpan> spansCopy;
      synchronized (monitor) {
        spansCopy = new ArrayList<>(spansList);
        spansList.clear();
      }
      // Execute the batch export outside the synchronized to not block all producers.
      exportBatches(spansCopy);
    }

    private void exportBatches(ArrayList<ReadableSpan> spanList) {
      // TODO: Record a counter for pushed spans.
      for (int i = 0; i < spanList.size(); ) {
        int batchSizeLimit = Math.min(i + maxExportBatchSize, spanList.size());
        onBatchExport(createSpanDataForExport(spanList, i, batchSizeLimit));
        i = batchSizeLimit;
      }
    }

    private static List<SpanData> createSpanDataForExport(
        List<ReadableSpan> spanList, int startIndex, int numberToTake) {
      List<SpanData> spanDataBuffer = new ArrayList<>(numberToTake);
      for (int i = startIndex; i < numberToTake; i++) {
        spanDataBuffer.add(spanList.get(i).toSpanData());
        // Remove the reference to the ReadableSpan to allow GC to free the memory.
        spanList.set(i, null);
      }
      return Collections.unmodifiableList(spanDataBuffer);
    }

    // Exports the list of SpanData to the SpanExporter.
    private void onBatchExport(final List<SpanData> spans) {
      Future<?> submission =
          executorService.submit(
              new Runnable() {
                @Override
                public void run() {
                  // In case of any exception thrown by the service handlers catch and log.
                  try {
                    spanExporter.export(spans);
                  } catch (Throwable t) {
                    logger.log(Level.WARNING, "Exception thrown by the export.", t);
                  }
                }
              });
      try {
        // wait at most for the configured timeout.
        submission.get(exporterTimeoutMillis, TimeUnit.MILLISECONDS);
      } catch (InterruptedException | ExecutionException e) {
        logger.log(Level.WARNING, "Exception thrown by the export.", e);
      } catch (TimeoutException e) {
        logger.log(Level.WARNING, "Export timed out. Cancelling execution.", e);
        submission.cancel(true);
      }
    }
  }
}
