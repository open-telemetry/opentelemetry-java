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

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.DaemonThreadFactory;
import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Implementation of the {@link SpanProcessor} that batches spans exported by the SDK then pushes
 * them to the exporter pipeline.
 *
 * <p>All spans reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full spans are dropped). Spans are exported either
 * when there are {@code maxExportBatchSize} pending spans or {@code scheduleDelayMillis} has passed
 * since the last export finished.
 *
 * <p>This batch {@link SpanProcessor} can cause high contention in a very high traffic service.
 * TODO: Add a link to the SpanProcessor that uses Disruptor as alternative with low contention.
 *
 * <p>Configuration options for {@link BatchSpanProcessor} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link BatchSpanProcessor}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.bsp.schedule.delay}: sets the delay interval between two consecutive exports.
 *   <li>{@code otel.bsp.max.queue}: sets the maximum queue size.
 *   <li>{@code otel.bsp.max.export.batch}: sets the maximum batch size.
 *   <li>{@code otel.bsp.export.timeout}: sets the maximum allowed time to export data.
 *   <li>{@code otel.bsp.export.sampled}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <p>For environment variables, {@link BatchSpanProcessor} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_BSP_SCHEDULE_DELAY}: sets the delay interval between two consecutive exports.
 *   <li>{@code OTEL_BSP_MAX_QUEUE}: sets the maximum queue size.
 *   <li>{@code OTEL_BSP_MAX_EXPORT_BATCH}: sets the maximum batch size.
 *   <li>{@code OTEL_BSP_EXPORT_TIMEOUT}: sets the maximum allowed time to export data.
 *   <li>{@code OTEL_BSP_EXPORT_SAMPLED}: sets whether only sampled spans should be exported.
 * </ul>
 */
public final class BatchSpanProcessor implements SpanProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchSpanProcessor.class.getSimpleName() + "_WorkerThread";
  private final Worker worker;
  private final boolean sampled;

  private BatchSpanProcessor(
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
            maxExportBatchSize,
            exporterTimeoutMillis,
            new ArrayBlockingQueue<ReadableSpan>(maxQueueSize));
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
    this.sampled = sampled;
  }

  @Override
  public void onStart(ReadWriteSpan span) {}

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
    worker.shutdown();
  }

  @Override
  public void forceFlush() {
    worker.forceFlush();
  }

  // Worker is a thread that batches multiple spans and calls the registered SpanExporter to export
  // the data.
  private static final class Worker implements Runnable {

    static {
      Meter meter = OpenTelemetry.getMeter("io.opentelemetry.sdk.trace");
      LongCounter droppedSpansCounter =
          meter
              .longCounterBuilder("droppedSpans")
              .setUnit("1")
              .setDescription(
                  "The number of spans dropped by the BatchSpanProcessor due to high throughput.")
              .build();
      droppedSpans =
          droppedSpansCounter.bind(
              Labels.of("spanProcessorType", BatchSpanProcessor.class.getSimpleName()));
    }

    private static final BoundLongCounter droppedSpans;

    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private final SpanExporter spanExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final int exporterTimeoutMillis;

    private long nextExportTime;

    private final BlockingQueue<ReadableSpan> queue;

    @Nullable private volatile CompletableResultCode flushRequested = null;
    private volatile boolean continueWork = true;
    private final ArrayList<SpanData> batch;

    private Worker(
        SpanExporter spanExporter,
        long scheduleDelayMillis,
        int maxExportBatchSize,
        int exporterTimeoutMillis,
        BlockingQueue<ReadableSpan> queue) {
      this.spanExporter = spanExporter;
      this.scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(scheduleDelayMillis);
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      this.queue = queue;
      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addSpan(ReadableSpan span) {
      if (!queue.offer(span)) {
        droppedSpans.add(1);
      }
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        try {
          if (flushRequested != null) {
            flush();
          }

          ReadableSpan lastElement = queue.poll(100, TimeUnit.MILLISECONDS);
          if (lastElement != null) {
            batch.add(lastElement.toSpanData());
          }

          if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
            exportCurrentBatch();
            updateNextExportTime();
          }

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }

    private void flush() {
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
      Objects.requireNonNull(flushRequested).succeed();
      flushRequested = null;
    }

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    private void shutdown() {
      forceFlush();
      spanExporter.shutdown();
      continueWork = false;
    }

    private void forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      this.flushRequested = flushResult;
      try {
        flushResult.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private void exportCurrentBatch() {
      if (batch.isEmpty()) {
        return;
      }

      try {
        final CompletableResultCode result = spanExporter.export(batch);
        result.join(exporterTimeoutMillis, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
          logger.log(Level.FINE, "Exporter failed");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.log(Level.WARNING, "Export timed out", e);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Exporter threw an Exception", e);
      } finally {
        batch.clear();
      }
    }
  }

  /**
   * Returns a new Builder for {@link BatchSpanProcessor}.
   *
   * @param spanExporter the {@code SpanExporter} to where the Spans are pushed.
   * @return a new {@link BatchSpanProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static Builder newBuilder(SpanExporter spanExporter) {
    return new Builder(spanExporter);
  }

  /** Builder class for {@link BatchSpanProcessor}. */
  public static final class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_SCHEDULE_DELAY_MILLIS = "otel.bsp.schedule.delay";
    private static final String KEY_MAX_QUEUE_SIZE = "otel.bsp.max.queue";
    private static final String KEY_MAX_EXPORT_BATCH_SIZE = "otel.bsp.max.export.batch";
    private static final String KEY_EXPORT_TIMEOUT_MILLIS = "otel.bsp.export.timeout";
    private static final String KEY_SAMPLED = "otel.bsp.export.sampled";

    @VisibleForTesting static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 5000;
    @VisibleForTesting static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
    @VisibleForTesting static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
    @VisibleForTesting static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;
    @VisibleForTesting static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;

    private final SpanExporter spanExporter;
    private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
    private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
    private int exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;
    private boolean exportOnlySampled = DEFAULT_EXPORT_ONLY_SAMPLED;

    private Builder(SpanExporter spanExporter) {
      this.spanExporter = Utils.checkNotNull(spanExporter, "spanExporter");
    }

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     */
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, Builder.NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Long longValue = getLongProperty(KEY_SCHEDULE_DELAY_MILLIS, configMap);
      if (longValue != null) {
        this.setScheduleDelayMillis(longValue);
      }
      Integer intValue = getIntProperty(KEY_MAX_QUEUE_SIZE, configMap);
      if (intValue != null) {
        this.setMaxQueueSize(intValue);
      }
      intValue = getIntProperty(KEY_MAX_EXPORT_BATCH_SIZE, configMap);
      if (intValue != null) {
        this.setMaxExportBatchSize(intValue);
      }
      intValue = getIntProperty(KEY_EXPORT_TIMEOUT_MILLIS, configMap);
      if (intValue != null) {
        this.setExporterTimeoutMillis(intValue);
      }
      Boolean boolValue = getBooleanProperty(KEY_SAMPLED, configMap);
      if (boolValue != null) {
        this.setExportOnlySampled(boolValue);
      }
      return this;
    }

    // TODO: Consider to add support for constant Attributes and/or Resource.

    /**
     * Set whether only sampled spans should be reported.
     *
     * <p>Default value is {@code true}.
     *
     * @param exportOnlySampled if {@code true} report only sampled spans.
     * @return this.
     * @see BatchSpanProcessor.Builder#DEFAULT_EXPORT_ONLY_SAMPLED
     */
    public Builder setExportOnlySampled(boolean exportOnlySampled) {
      this.exportOnlySampled = exportOnlySampled;
      return this;
    }

    @VisibleForTesting
    boolean getExportOnlySampled() {
      return exportOnlySampled;
    }

    /**
     * Sets the delay interval between two consecutive exports. The actual interval may be shorter
     * if the batch size is getting larger than {@code maxQueuedSpans / 2}.
     *
     * <p>Default value is {@code 5000}ms.
     *
     * @param scheduleDelayMillis the delay interval between two consecutive exports.
     * @return this.
     * @see BatchSpanProcessor.Builder#DEFAULT_SCHEDULE_DELAY_MILLIS
     */
    public Builder setScheduleDelayMillis(long scheduleDelayMillis) {
      this.scheduleDelayMillis = scheduleDelayMillis;
      return this;
    }

    @VisibleForTesting
    long getScheduleDelayMillis() {
      return scheduleDelayMillis;
    }

    /**
     * Sets the maximum time an exporter will be allowed to run before being cancelled.
     *
     * <p>Default value is {@code 30000}ms
     *
     * @param exporterTimeoutMillis the timeout for exports in milliseconds.
     * @return this
     * @see BatchSpanProcessor.Builder#DEFAULT_EXPORT_TIMEOUT_MILLIS
     */
    public Builder setExporterTimeoutMillis(int exporterTimeoutMillis) {
      this.exporterTimeoutMillis = exporterTimeoutMillis;
      return this;
    }

    @VisibleForTesting
    int getExporterTimeoutMillis() {
      return exporterTimeoutMillis;
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
     * @see BatchSpanProcessor.Builder#DEFAULT_MAX_QUEUE_SIZE
     */
    public Builder setMaxQueueSize(int maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
      return this;
    }

    @VisibleForTesting
    int getMaxQueueSize() {
      return maxQueueSize;
    }

    /**
     * Sets the maximum batch size for every export. This must be smaller or equal to {@code
     * maxQueuedSpans}.
     *
     * <p>Default value is {@code 512}.
     *
     * @param maxExportBatchSize the maximum batch size for every export.
     * @return this.
     * @see BatchSpanProcessor.Builder#DEFAULT_MAX_EXPORT_BATCH_SIZE
     */
    public Builder setMaxExportBatchSize(int maxExportBatchSize) {
      Utils.checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
      this.maxExportBatchSize = maxExportBatchSize;
      return this;
    }

    @VisibleForTesting
    int getMaxExportBatchSize() {
      return maxExportBatchSize;
    }

    /**
     * Returns a new {@link BatchSpanProcessor} that batches, then converts spans to proto and
     * forwards them to the given {@code spanExporter}.
     *
     * @return a new {@link BatchSpanProcessor}.
     * @throws NullPointerException if the {@code spanExporter} is {@code null}.
     */
    public BatchSpanProcessor build() {
      return new BatchSpanProcessor(
          spanExporter,
          exportOnlySampled,
          scheduleDelayMillis,
          maxQueueSize,
          maxExportBatchSize,
          exporterTimeoutMillis);
    }
  }
}
