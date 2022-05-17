/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.Collection;

/**
 * A reader of metrics from {@link SdkMeterProvider}.
 *
 * <p>Custom implementations of {@link MetricReader} are not currently supported. Please use one of
 * the built-in readers such as {@link PeriodicMetricReader}.
 */
public interface MetricReader extends AggregationTemporalitySelector, DefaultAggregationSelector {

  /**
   * Called by {@link SdkMeterProvider} and supplies the {@link MetricReader} with a handle to
   * collect metrics.
   *
   * <p>{@link CollectionRegistration} is currently an empty interface because custom
   * implementations of {@link MetricReader} are not currently supported.
   */
  void register(CollectionRegistration registration);

  /**
   * Return the default aggregation for the {@link InstrumentType}.
   *
   * @see DefaultAggregationSelector#getDefaultAggregation(InstrumentType)
   */
  @Override
  default Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return Aggregation.defaultAggregation();
  }

  /**
   * Flushes metrics read by this reader.
   *
   * <p>In all scenarios, the trigger a metrics collection.
   *
   * <p>For readers associated with push {@link MetricExporter}s, this should {@link
   * MetricExporter#export(Collection)} the collected metrics.
   *
   * @return the result of the flush.
   */
  CompletableResultCode forceFlush();

  /**
   * Shuts down the metric reader.
   *
   * <p>For pull endpoints, like prometheus, this should shut down the metric hosting endpoint or
   * server doing such a job.
   *
   * <p>For push endpoints, this should shut down any scheduler threads.
   *
   * @return the result of the shutdown.
   */
  CompletableResultCode shutdown();
}
