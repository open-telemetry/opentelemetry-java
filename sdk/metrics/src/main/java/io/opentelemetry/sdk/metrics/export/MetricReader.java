/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

/**
 * A metric reader reads metrics from an {@link SdkMeterProvider}.
 *
 * <p>Custom implementations of {@link MetricReader} are not currently supported. Please use one of
 * the built-in readers such as {@link PeriodicMetricReader}.
 *
 * @since 1.14.0
 */
public interface MetricReader extends AggregationTemporalitySelector {

  /**
   * Called by {@link SdkMeterProvider} and supplies the {@link MetricReader} with a handle to
   * collect metrics.
   *
   * <p>{@link CollectionRegistration} is currently an empty interface because custom
   * implementations of {@link MetricReader} are not currently supported.
   */
  void register(CollectionRegistration registration);

  /**
   * Read and export the metrics.
   *
   * <p>Called when {@link SdkMeterProvider#forceFlush()} is called.
   *
   * @return the result of the flush.
   */
  CompletableResultCode forceFlush();

  /**
   * Shuts down the metric reader.
   *
   * <p>Called when {@link SdkMeterProvider#shutdown()} is called.
   *
   * <p>For pull based readers like prometheus, this should shut down the metric hosting endpoint or
   * server doing such a job.
   *
   * <p>For push based readers like {@link MetricExporter}, this should shut down any scheduler
   * threads.
   *
   * @return the result of the shutdown.
   */
  CompletableResultCode shutdown();
}
