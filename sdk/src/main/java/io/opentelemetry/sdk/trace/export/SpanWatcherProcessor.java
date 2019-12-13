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
import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

/**
 * Implementation of the {@link SpanProcessor} that repeatedly reports active spans that are older
 *
 * <p>All spans started by the SDK are first added to a synchronized list (with a {@code
 * maxQueueSize} maximum size, after the size is reached spans are dropped) and exported every
 * {@code reportIntervalMillis} to the exporter pipeline in batches of {@code maxExportBatchSize}.
 *
 * <p>If the queue gets half full a preemptive notification is sent to the worker thread that
 * exports the spans to wake up and start a new export cycle.
 *
 * <p>This batch {@link SpanProcessor} can cause high contention in a very high traffic service.
 * TODO: Add a link to the SpanProcessor that uses Disruptor as alternative with low contention.
 */
public final class SpanWatcherProcessor implements SpanProcessor {
  private static final String WORKER_THREAD_NAME =
      SpanWatcherProcessor.class.getSimpleName() + "_WorkerThread";
  private final Worker worker;
  private final Thread workerThread;
  private final boolean sampled;

  private SpanWatcherProcessor(
      SpanExporter spanExporter,
      boolean sampled,
      long reportIntervalMillis,
      int maxQueueSize,
      int maxExportBatchSize) {
    this.worker = new Worker(spanExporter, reportIntervalMillis, maxQueueSize, maxExportBatchSize);
    this.workerThread = newThread(worker);
    this.workerThread.start();
    this.sampled = sampled;
  }

  @Override
  public void onStart(ReadableSpan span) {
    if (sampled && !span.getSpanContext().getTraceFlags().isSampled()) {
      return;
    }
    worker.addSpan(span);
  }

  @Override
  public void onEnd(ReadableSpan span) {}

  @Override
  public void shutdown() {
    workerThread.interrupt();
    worker.flush();
  }

  /**
   * Returns a new Builder for {@link SpanWatcherProcessor}.
   *
   * @param spanExporter the {@code SpanExporter} to where the Spans are pushed.
   * @return a new {@link SpanWatcherProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static Builder newBuilder(SpanExporter spanExporter) {
    return new Builder(spanExporter);
  }

  /** Builder class for {@link SpanWatcherProcessor}. */
  public static final class Builder {
    private static final long REPORT_INTERVAL_MILLIS = 5000;
    private static final int MAX_QUEUE_SIZE = 2048;
    private static final int MAX_EXPORT_BATCH_SIZE = 512;
    private final SpanExporter spanExporter;
    private long reportIntervalMillis = REPORT_INTERVAL_MILLIS;
    private int maxWatchlistSize = MAX_QUEUE_SIZE;
    private int maxExportBatchSize = MAX_EXPORT_BATCH_SIZE;
    private boolean reportOnlySampled = true;

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
      this.reportOnlySampled = sampled;
      return this;
    }

    /**
     * Sets the delay interval between two consecutive exports. The actual interval may be shorter
     * if the batch size is getting larger than {@code maxQueuedSpans / 2}.
     *
     * <p>Default value is {@code 5000}ms.
     *
     * @param reportIntervalMillis the delay interval between two consecutive exports.
     * @return this.
     */
    public Builder setReportIntervalMillis(long reportIntervalMillis) {
      this.reportIntervalMillis = reportIntervalMillis;
      return this;
    }

    /**
     * Sets the maximum number of Spans that are kept in the watchlist before start dropping.
     *
     * <p>Default value is {@code 2048}.
     *
     * @param maxWatchlistSize the maximum number of Spans that are kept in the watchlist before
     *     spans are dropped.
     * @return this.
     */
    public Builder setMaxWatchlistSize(int maxWatchlistSize) {
      this.maxWatchlistSize = maxWatchlistSize;
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
     * Returns a new {@link SpanWatcherProcessor}.
     *
     * @return a new {@link SpanWatcherProcessor}.
     * @throws NullPointerException if the {@code spanExporter} is {@code null}.
     */
    public SpanWatcherProcessor build() {
      return new SpanWatcherProcessor(
          spanExporter,
          reportOnlySampled,
          reportIntervalMillis,
          maxWatchlistSize,
          maxExportBatchSize);
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
    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private final SpanExporter spanExporter;
    private final long reportIntervalMillis;
    private final int maxWatchlistSize;
    private final int maxExportBatchSize;
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<WeakReference<ReadableSpan>> spanWatchlist;

    private Worker(
        SpanExporter spanExporter,
        long reportIntervalMillis,
        int maxWatchlistSize,
        int maxExportBatchSize) {
      this.spanExporter = spanExporter;
      this.reportIntervalMillis = reportIntervalMillis;
      this.maxWatchlistSize = maxWatchlistSize;
      this.maxExportBatchSize = maxExportBatchSize;
      this.spanWatchlist = new ArrayList<>(maxWatchlistSize);
    }

    private void addSpan(ReadableSpan span) {
      synchronized (monitor) {
        if (spanWatchlist.size() == maxWatchlistSize) {
          // TODO: Record a counter for dropped spans.
          return;
        }
        // TODO: Record a gauge for referenced spans.
        spanWatchlist.add(new WeakReference<>(span));
      }

      // TODO: We should keep track of spans that have ended but weren't yet removed
      //  from spanList to clean up if that's the case.
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        // Copy all the batched spans in a separate list to release the monitor lock asap to
        // avoid blocking the producer thread.
        ArrayList<SpanData> unfinishedSpans;
        synchronized (monitor) {
          do {
            // In the case of a spurious wakeup we export only if we have at least one span in
            // the batch. It is acceptable because batching is a best effort mechanism here.
            try {
              monitor.wait(reportIntervalMillis);
            } catch (InterruptedException ie) {
              // Preserve the interruption status as per guidance and stop doing any work.
              Thread.currentThread().interrupt();
              return;
            }
          } while (spanWatchlist.isEmpty());
          unfinishedSpans = getUnfinishedSpans();
        }
        // Execute the batch export outside the synchronized to not block all producers.
        exportBatches(unfinishedSpans);
      }
    }

    @GuardedBy("monitor")
    private ArrayList<SpanData> getUnfinishedSpans() {
      ArrayList<SpanData> unfinishedSpans = new ArrayList<>();
      final long curTimeNanos = System.currentTimeMillis() * 1000L * 1000L;
      for (int i = 0; i < spanWatchlist.size(); ) {
        ReadableSpan span = spanWatchlist.get(i).get();
        if (span == null) {
          dropSpan(i);
          continue;
        }
        SpanData data = span.toSpanData();
        if (data.getEndEpochNanos() != 0) {
          dropSpan(i);
          continue;
        }
        // We could also use the time we add()ed the span, but since only in-band spans are supposed
        // to be reported,
        // using the start timestamp makes just as much sense.
        final long age = curTimeNanos - data.getStartEpochNanos();
        if (age > reportIntervalMillis) {
          // Many spans will be end()ed so soon that it won't bring much benefit to report them
          // earlier.
          unfinishedSpans.add(data);
        }
        ++i;
      }
      return unfinishedSpans;
    }

    @GuardedBy("monitor")
    private void dropSpan(int i) {
      // We don't care about the order of Spans in spanWatchlist,
      // so just this is more efficient than just using remove(i).

      final int lastIdx = spanWatchlist.size() - 1;
      if (i != lastIdx) {
        spanWatchlist.set(i, spanWatchlist.get(lastIdx));
      }
      spanWatchlist.remove(lastIdx);
    }

    private void flush() {
      ArrayList<SpanData> unfinishedSpans;
      synchronized (monitor) {
        unfinishedSpans = getUnfinishedSpans();
      }
      // Execute the batch export outside the synchronized to not block all producers.
      exportBatches(unfinishedSpans);
    }

    private void exportBatches(ArrayList<SpanData> spanList) {
      // TODO: Record a counter for pushed spans.
      for (int i = 0; i < spanList.size(); ) {
        int batchSizeLimit = Math.min(i + maxExportBatchSize, spanList.size());
        onBatchExport(createSpanDataForExport(spanList, i, batchSizeLimit));
        i = batchSizeLimit;
      }
    }

    private static List<SpanData> createSpanDataForExport(
        ArrayList<SpanData> spanList, int startIndex, int numberToTake) {
      List<SpanData> spanDataBuffer = new ArrayList<>(numberToTake);
      for (int i = startIndex; i < numberToTake; i++) {
        spanDataBuffer.add(spanList.get(i));
        // Remove the reference to the SpanData to allow GC to free the memory.
        spanList.set(i, null);
      }
      return Collections.unmodifiableList(spanDataBuffer);
    }

    // Exports the list of Span protos to all the ServiceHandlers.
    private void onBatchExport(List<SpanData> spans) {
      // In case of any exception thrown by the service handlers continue to run.
      try {
        spanExporter.export(spans);
      } catch (Throwable t) {
        logger.log(Level.WARNING, "Exception thrown by the export.", t);
      }
    }
  }
}
