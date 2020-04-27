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

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

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
    this.workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    this.workerThread.start();
    this.sampled = sampled;
  }

  /**
   * Creates a {@code BatchSpansProcessor} instance using the default configuration.
   *
   * @param spanExporter The {@link SpanExporter} to use
   * @return a {@code BatchSpansProcessor} instance.
   */
  public static BatchSpansProcessor create(SpanExporter spanExporter) {
    return create(spanExporter, Config.getDefault());
  }

  /**
   * Creates a {@code BatchSpansProcessor} instance.
   *
   * @param spanExporter The {@link SpanExporter} to use
   * @param config The {@link Config} to use
   * @return a {@code BatchSpansProcessor} instance.
   */
  public static BatchSpansProcessor create(SpanExporter spanExporter, Config config) {
    return new BatchSpansProcessor(
        spanExporter,
        config.isExportOnlySampled(),
        config.getScheduleDelayMillis(),
        config.getMaxQueueSize(),
        config.getMaxExportBatchSize(),
        config.getExporterTimeoutMillis());
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
      this.spanExporter = Objects.requireNonNull(spanExporter, "spanExporter");
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

  // Worker is a thread that batches multiple spans and calls the registered SpanExporter to export
  // the data.
  //
  // The list of batched data is protected by an explicit monitor object which ensures full
  // concurrency.
  private static final class Worker implements Runnable {

    static {
      Meter meter = OpenTelemetry.getMeterProvider().get("io.opentelemetry.sdk.trace");
      LongCounter droppedSpansCounter =
          meter
              .longCounterBuilder("droppedSpans")
              .setMonotonic(true)
              .setUnit("1")
              .setDescription(
                  "The number of spans dropped by the BatchSpansProcessor due to high throughput.")
              .build();
      droppedSpans =
          droppedSpansCounter.bind("spanProcessorType", BatchSpansProcessor.class.getSimpleName());
    }

    private static final BoundLongCounter droppedSpans;

    private final ExecutorService executorService =
        Executors.newSingleThreadExecutor(new DaemonThreadFactory(EXPORTER_THREAD_NAME));

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
      spanExporter.shutdown();
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

  @Immutable
  @AutoValue
  public abstract static class Config {

    private static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 5000;
    private static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
    private static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
    private static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;
    private static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;

    public abstract boolean isExportOnlySampled();

    public abstract long getScheduleDelayMillis();

    public abstract int getMaxQueueSize();

    public abstract int getMaxExportBatchSize();

    public abstract int getExporterTimeoutMillis();

    /**
     * Creates a {@link Config} object using the default configuration.
     *
     * @return The {@link Config} object.
     * @since 0.4.0
     */
    public static Config getDefault() {
      return newBuilder().build();
    }

    /**
     * Creates a {@link Config} object reading the configuration values from the environment and
     * from system properties. System properties override values defined in the environment. If a
     * configuration value is missing, it uses the default value.
     *
     * @return The {@link Config} object.
     * @since 0.4.0
     */
    public static Config loadFromDefaultSources() {
      return newBuilder().readEnvironment().readSystemProperties().build();
    }

    /**
     * Returns a new {@link Builder} with default options.
     *
     * @return a new {@code Builder} with default options.
     * @since 0.4.0
     */
    public static Builder newBuilder() {
      return new AutoValue_BatchSpansProcessor_Config.Builder()
          .setScheduleDelayMillis(DEFAULT_SCHEDULE_DELAY_MILLIS)
          .setMaxQueueSize(DEFAULT_MAX_QUEUE_SIZE)
          .setMaxExportBatchSize(DEFAULT_MAX_EXPORT_BATCH_SIZE)
          .setExporterTimeoutMillis(DEFAULT_EXPORT_TIMEOUT_MILLIS)
          .setExportOnlySampled(DEFAULT_EXPORT_ONLY_SAMPLED);
    }

    @AutoValue.Builder
    public abstract static class Builder {

      private static final String KEY_SCHEDULE_DELAY_MILLIS = "otel.bsp.schedule.delay";
      private static final String KEY_MAX_QUEUE_SIZE = "otel.bsp.max.queue";
      private static final String KEY_MAX_EXPORT_BATCH_SIZE = "otel.bsp.max.export.batch";
      private static final String KEY_EXPORT_TIMEOUT_MILLIS = "otel.bsp.export.timeout";
      private static final String KEY_SAMPLED = "otel.bsp.export.sampled";

      @VisibleForTesting
      protected enum NamingConvention {
        DOT {
          @Override
          public String normalize(@Nonnull String key) {
            return key.toLowerCase();
          }
        },
        ENV_VAR {
          @Override
          public String normalize(@Nonnull String key) {
            return key.toLowerCase().replace("_", ".");
          }
        };

        public abstract String normalize(@Nonnull String key);

        public Map<String, String> normalize(@Nonnull Map<String, String> map) {
          Map<String, String> properties = new HashMap<>();
          for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.put(normalize(entry.getKey()), entry.getValue());
          }
          return Collections.unmodifiableMap(properties);
        }
      }

      /**
       * Sets the configuration values from the given configuration map for only the available keys.
       * This method looks for the following keys:
       *
       * <ul>
       *   <li>{@code otel.bsp.schedule.delay}: to set the delay interval between two consecutive
       *       exports.
       *   <li>{@code otel.bsp.max.queue}: to set the maximum queue size.
       *   <li>{@code otel.bsp.max.export.batch}: to set the maximum batch size.
       *   <li>{@code otel.bsp.export.timeout}: to set the maximum allowed time to export data.
       *   <li>{@code otel.bsp.export.sampled}: to set whether only sampled spans should be
       *       exported.
       * </ul>
       *
       * @param configMap {@link Map} holding the configuration values.
       * @return this.
       */
      @VisibleForTesting
      Builder fromConfigMap(Map<String, String> configMap, NamingConvention namingConvention) {
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

      /**
       * Sets the configuration values from the given properties object for only the available keys.
       * This method looks for the following keys:
       *
       * <ul>
       *   <li>{@code otel.bsp.schedule.delay}: to set the delay interval between two consecutive
       *       exports.
       *   <li>{@code otel.bsp.max.queue}: to set the maximum queue size.
       *   <li>{@code otel.bsp.max.export.batch}: to set the maximum batch size.
       *   <li>{@code otel.bsp.export.timeout}: to set the maximum allowed time to export data.
       *   <li>{@code otel.bsp.export.sampled}: to set whether only sampled spans should be
       *       exported.
       * </ul>
       *
       * @param properties {@link Properties} holding the configuration values.
       * @return this.
       */
      public Builder readProperties(Properties properties) {
        return fromConfigMap(Maps.fromProperties(properties), NamingConvention.DOT);
      }

      /**
       * Sets the configuration values from environment variables for only the available keys. This
       * method looks for the following keys:
       *
       * <ul>
       *   <li>{@code OTEL_BSP_SCHEDULE_DELAY}: to set the delay interval between two consecutive
       *       exports.
       *   <li>{@code OTEL_BSP_MAX_QUEUE}: to set the maximum queue size.
       *   <li>{@code OTEL_BSP_MAX_EXPORT_BATCH}: to set the maximum batch size.
       *   <li>{@code OTEL_BSP_EXPORT_TIMEOUT}: to set the maximum allowed time to export data.
       *   <li>{@code OTEL_BSP_EXPORT_SAMPLED}: to set whether only sampled spans should be
       *       exported.
       * </ul>
       *
       * @return this.
       */
      public Builder readEnvironment() {
        return fromConfigMap(System.getenv(), NamingConvention.ENV_VAR);
      }

      /**
       * Sets the configuration values from system properties for only the available keys. This
       * method looks for the following keys:
       *
       * <ul>
       *   <li>{@code otel.bsp.schedule.delay}: to set the delay interval between two consecutive
       *       exports.
       *   <li>{@code otel.bsp.max.queue}: to set the maximum queue size.
       *   <li>{@code otel.bsp.max.export.batch}: to set the maximum batch size.
       *   <li>{@code otel.bsp.export.timeout}: to set the maximum allowed time to export data.
       *   <li>{@code otel.bsp.export.sampled}: to set whether only sampled spans should be
       *       reported.
       * </ul>
       *
       * @return this.
       */
      public Builder readSystemProperties() {
        return readProperties(System.getProperties());
      }

      /**
       * Set whether only sampled spans should be exported.
       *
       * <p>Default value is {@code true}.
       *
       * @param sampled report only sampled spans.
       * @return this.
       * @see BatchSpansProcessor.Config#DEFAULT_EXPORT_ONLY_SAMPLED
       */
      public abstract Builder setExportOnlySampled(boolean sampled);

      /**
       * Sets the delay interval between two consecutive exports. The actual interval may be shorter
       * if the batch size is getting larger than {@code maxQueuedSpans / 2}.
       *
       * <p>Default value is {@code 5000}ms.
       *
       * @param scheduleDelayMillis the delay interval between two consecutive exports.
       * @return this.
       * @see BatchSpansProcessor.Config#DEFAULT_SCHEDULE_DELAY_MILLIS
       */
      public abstract Builder setScheduleDelayMillis(long scheduleDelayMillis);

      /**
       * Sets the maximum time an exporter will be allowed to run before being cancelled.
       *
       * <p>Default value is {@code 30000}ms
       *
       * @param exporterTimeoutMillis the timeout for exports in milliseconds.
       * @return this
       * @see BatchSpansProcessor.Config#DEFAULT_EXPORT_TIMEOUT_MILLIS
       */
      public abstract Builder setExporterTimeoutMillis(int exporterTimeoutMillis);

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
       * @see BatchSpansProcessor.Config#DEFAULT_MAX_QUEUE_SIZE
       */
      public abstract Builder setMaxQueueSize(int maxQueueSize);

      /**
       * Sets the maximum batch size for every export. This must be smaller or equal to {@code
       * maxQueuedSpans}.
       *
       * <p>Default value is {@code 512}.
       *
       * @param maxExportBatchSize the maximum batch size for every export.
       * @return this.
       * @see BatchSpansProcessor.Config#DEFAULT_MAX_EXPORT_BATCH_SIZE
       */
      public abstract Builder setMaxExportBatchSize(int maxExportBatchSize);

      abstract Config autoBuild(); // not public

      /**
       * Builds the {@link Config} object.
       *
       * @return the {@link Config} object.
       */
      public Config build() {
        Config config = autoBuild();
        Utils.checkArgument(
            config.getScheduleDelayMillis() >= 0,
            "scheduleDelayMillis must greater than or equal 0.");
        Utils.checkArgument(
            config.getExporterTimeoutMillis() >= 0,
            "exporterTimeoutMillis must greater than or equal 0.");
        Utils.checkArgument(config.getMaxQueueSize() > 0, "maxQueueSize must be positive.");
        Utils.checkArgument(
            config.getMaxExportBatchSize() > 0, "maxExportBatchSize must be positive.");
        return config;
      }

      @Nullable
      private static Boolean getBooleanProperty(String name, Map<String, String> map) {
        if (map.containsKey(name)) {
          return Boolean.parseBoolean(map.get(name));
        }
        return null;
      }

      @Nullable
      private static Integer getIntProperty(String name, Map<String, String> map) {
        try {
          return Integer.parseInt(map.get(name));
        } catch (NumberFormatException ex) {
          return null;
        }
      }

      @Nullable
      private static Long getLongProperty(String name, Map<String, String> map) {
        try {
          return Long.parseLong(map.get(name));
        } catch (NumberFormatException ex) {
          return null;
        }
      }
    }
  }
}
