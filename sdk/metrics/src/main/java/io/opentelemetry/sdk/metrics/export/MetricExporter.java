/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.EnumSet;

/**
 * {@code MetricExporter} is the interface that all "push based" metric libraries should use to
 * export metrics to the OpenTelemetry exporters.
 *
 * <p>All OpenTelemetry exporters should allow access to a {@code MetricExporter} instance.
 */
public interface MetricExporter {

  /** Returns the set of all supported temporalities for this exporter. */
  EnumSet<AggregationTemporality> getSupportedTemporality();

  /** Returns the preferred temporality for metrics. */
  AggregationTemporality getPreferedTemporality();

  /**
   * Exports the collection of given {@link MetricData}. Note that export operations can be
   * performed simultaneously depending on the type of metric reader being used. However, the caller
   * MUST ensure that only one export can occur at a time.
   *
   * @param metrics the collection of {@link MetricData} to be exported.
   * @return the result of the export, which is often an asynchronous operation.
   */
  CompletableResultCode export(Collection<MetricData> metrics);

  /**
   * Exports the collection of {@link MetricData} that have not yet been exported. Note that flush
   * operations can be performed simultaneously depending on the type of metric reader being used.
   * However, the caller MUST ensure that only one export can occur at a time.
   *
   * @return the result of the flush, which is often an asynchronous operation.
   */
  CompletableResultCode flush();

  /**
   * Called when the associated IntervalMetricReader is shutdown.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes.
   */
  CompletableResultCode shutdown();
}
