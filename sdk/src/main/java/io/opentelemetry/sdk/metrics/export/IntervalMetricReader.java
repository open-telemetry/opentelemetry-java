/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.metrics.export;

import com.google.auto.value.AutoValue;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.common.DaemonThreadFactory;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/**
 * Wraps a list of {@link MetricProducer}s and automatically reads and exports the metrics every
 * export interval.
 *
 * @since 0.3.0
 */
public final class IntervalMetricReader {
  private final Exporter exporter;
  private final ScheduledExecutorService scheduler;

  /**
   * Stops the scheduled task and calls export one more time.
   *
   * @since 0.3.0
   */
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
   * Builder for {@link IntervalMetricReader}.
   *
   * @since 0.3.0
   */
  public static final class Builder {
    private final InternalState.Builder optionsBuilder;

    private Builder(InternalState.Builder optionsBuilder) {
      this.optionsBuilder = optionsBuilder;
    }

    /**
     * Sets the export interval.
     *
     * @param exportIntervalMillis the export interval between pushes to the exporter.
     * @return this.
     * @since 0.3.0
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
     * @since 0.3.0
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
     * @since 0.3.0
     */
    public Builder setMetricProducers(Collection<MetricProducer> metricProducers) {
      optionsBuilder.setMetricProducers(metricProducers);
      return this;
    }

    /**
     * Builds a new {@link IntervalMetricReader} with current settings.
     *
     * @return a {@code IntervalMetricReader}.
     * @since 0.3.0
     */
    public IntervalMetricReader build() {
      InternalState internalState = optionsBuilder.build();
      Utils.checkArgument(
          internalState.getExportIntervalMillis() > 0, "Export interval must be positive");

      return new IntervalMetricReader(internalState);
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

    private Exporter(InternalState internalState) {
      this.internalState = internalState;
    }

    @Override
    public void run() {
      List<MetricData> metricsList = new ArrayList<>();
      for (MetricProducer metricProducer : internalState.getMetricProducers()) {
        metricsList.addAll(metricProducer.getAllMetrics());
      }
      internalState.getMetricExporter().export(Collections.unmodifiableList(metricsList));
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
