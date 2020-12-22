/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Wraps a list of {@link MetricProducer}s and automatically reads and exports the metrics every
 * export interval. Metrics may also be dropped when it becomes time to export again, and there is
 * an export in progress.
 *
 * <p>Configuration options for {@link IntervalMetricReader} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link IntervalMetricReader}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.imr.export.interval}: sets the export interval between pushes to the exporter.
 * </ul>
 *
 * <p>For environment variables, {@link IntervalMetricReader} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_IMR_EXPORT_INTERVAL}: sets the export interval between pushes to the exporter.
 * </ul>
 */
public final class IntervalMetricReader {
  private static final Logger logger = Logger.getLogger(IntervalMetricReader.class.getName());

  private final Exporter exporter;
  private final ScheduledExecutorService scheduler;

  /** Stops the scheduled task and calls export one more time. */
  public void shutdown() {
    scheduler.shutdown();
    try {
      scheduler.awaitTermination(5, TimeUnit.SECONDS);
      exporter.run();
    } catch (InterruptedException e) {
      // force a shutdown if the export hasn't finished.
      scheduler.shutdownNow();
      // reset the interrupted status
      Thread.currentThread().interrupt();
    } finally {
      exporter.shutdown();
    }
  }

  /**
   * Returns a new {@link Builder} for {@link IntervalMetricReader}.
   *
   * @return a new {@link Builder} for {@link IntervalMetricReader}.
   */
  public static Builder builder() {
    return new Builder(InternalState.builder());
  }

  /**
   * Returns a new {@link Builder} for {@link IntervalMetricReader} reading the configuration values
   * from the environment and from system properties. System properties override values defined in
   * the environment. If a configuration value is missing, it uses the default value.
   *
   * @return a new {@link Builder} for {@link IntervalMetricReader}.
   */
  public static Builder builderFromDefaultSources() {
    return builder().readEnvironmentVariables().readSystemProperties();
  }

  /** Builder for {@link IntervalMetricReader}. */
  public static final class Builder extends ConfigBuilder<Builder> {
    private final InternalState.Builder optionsBuilder;
    private static final String KEY_EXPORT_INTERVAL = "otel.imr.export.interval";

    private Builder(InternalState.Builder optionsBuilder) {
      this.optionsBuilder = optionsBuilder;
    }

    /**
     * Sets the export interval.
     *
     * @param exportIntervalMillis the export interval between pushes to the exporter.
     * @return this.
     */
    public Builder setExportIntervalMillis(long exportIntervalMillis) {
      optionsBuilder.setExportIntervalMillis(exportIntervalMillis);
      return this;
    }

    /**
     * Sets the exporter to be called when export metrics.
     *
     * @param metricExporter the {@link MetricExporter} to be called when export metrics.
     * @return this.
     */
    public Builder setMetricExporter(MetricExporter metricExporter) {
      optionsBuilder.setMetricExporter(metricExporter);
      return this;
    }

    /**
     * Sets a collection of {@link MetricProducer} from where the metrics should be read.
     *
     * @param metricProducers a collection of {@link MetricProducer} from where the metrics should
     *     be read.
     * @return this.
     */
    public Builder setMetricProducers(Collection<MetricProducer> metricProducers) {
      optionsBuilder.setMetricProducers(metricProducers);
      return this;
    }

    /**
     * Builds a new {@link IntervalMetricReader} with current settings.
     *
     * @return a {@code IntervalMetricReader}.
     */
    public IntervalMetricReader build() {
      InternalState internalState = optionsBuilder.build();
      Utils.checkArgument(
          internalState.getExportIntervalMillis() > 0, "Export interval must be positive");

      return new IntervalMetricReader(internalState);
    }

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     */
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Long value = getLongProperty(KEY_EXPORT_INTERVAL, configMap);
      if (value != null) {
        this.setExportIntervalMillis(value);
      }
      return this;
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private IntervalMetricReader(InternalState internalState) {
    this.exporter = new Exporter(internalState);
    this.scheduler =
        Executors.newScheduledThreadPool(1, new DaemonThreadFactory("IntervalMetricReader"));
    this.scheduler.scheduleAtFixedRate(
        exporter,
        internalState.getExportIntervalMillis(),
        internalState.getExportIntervalMillis(),
        TimeUnit.MILLISECONDS);
  }

  private static final class Exporter implements Runnable {

    private final InternalState internalState;
    private final AtomicBoolean exportAvailable = new AtomicBoolean(true);

    private Exporter(InternalState internalState) {
      this.internalState = internalState;
    }

    @Override
    @SuppressWarnings("BooleanParameter")
    public void run() {
      if (exportAvailable.compareAndSet(true, false)) {
        try {
          List<MetricData> metricsList = new ArrayList<>();
          for (MetricProducer metricProducer : internalState.getMetricProducers()) {
            metricsList.addAll(metricProducer.collectAllMetrics());
          }
          final CompletableResultCode result =
              internalState.getMetricExporter().export(Collections.unmodifiableList(metricsList));
          result.whenComplete(
              () -> {
                if (!result.isSuccess()) {
                  logger.log(Level.FINE, "Exporter failed");
                }
                exportAvailable.set(true);
              });
        } catch (Exception e) {
          logger.log(Level.WARNING, "Exporter threw an Exception", e);
        }
      } else {
        logger.log(Level.FINE, "Exporter busy. Dropping metrics.");
      }
    }

    void shutdown() {
      internalState.getMetricExporter().shutdown();
    }
  }

  @AutoValue
  @Immutable
  abstract static class InternalState {
    static final long DEFAULT_INTERVAL_MILLIS = 60_000;

    abstract MetricExporter getMetricExporter();

    abstract long getExportIntervalMillis();

    abstract Collection<MetricProducer> getMetricProducers();

    static Builder builder() {
      return new AutoValue_IntervalMetricReader_InternalState.Builder()
          .setExportIntervalMillis(DEFAULT_INTERVAL_MILLIS);
    }

    @AutoValue.Builder
    abstract static class Builder {

      abstract Builder setExportIntervalMillis(long exportIntervalMillis);

      abstract Builder setMetricExporter(MetricExporter metricExporter);

      abstract Builder setMetricProducers(Collection<MetricProducer> metricProducers);

      abstract InternalState build();
    }
  }
}
