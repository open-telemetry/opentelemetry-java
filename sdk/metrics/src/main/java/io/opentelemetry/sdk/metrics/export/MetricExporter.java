/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.Closeable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A Metric Exporter is a push based interface for exporting {@link MetricData} out of {@link
 * SdkMeterProvider}.
 *
 * <p>To use, associate an exporter with a {@link PeriodicMetricReader}, and register with the
 * metrics SDK via {@link SdkMeterProviderBuilder#registerMetricReader(MetricReader)}.
 *
 * @since 1.14.0
 */
public interface MetricExporter
    extends AggregationTemporalitySelector, DefaultAggregationSelector, Closeable {

  /**
   * Return the default aggregation for the {@link InstrumentType}.
   *
   * @see DefaultAggregationSelector#getDefaultAggregation(InstrumentType)
   * @since 1.16.0
   */
  @Override
  default Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return Aggregation.defaultAggregation();
  }

  /**
   * Exports the {@code metrics}. The caller (i.e. {@link PeriodicMetricReader} will not call export
   * until the previous call completes.
   *
   * @param metrics the metrics to export.
   * @return the result of the export, which is often an asynchronous operation.
   */
  CompletableResultCode export(Collection<MetricData> metrics);

  /**
   * A hint that any metrics previously {@link #export(Collection)}ed should be completed.
   *
   * @return the result of the flush, which is often an asynchronous operation.
   */
  CompletableResultCode flush();

  /**
   * Shuts down the exporter.
   *
   * <p>Called when {@link PeriodicMetricReader#shutdown()} of the associated reader is called.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes.
   */
  CompletableResultCode shutdown();

  /** Closes this {@link MetricExporter}, releasing any resources. */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
