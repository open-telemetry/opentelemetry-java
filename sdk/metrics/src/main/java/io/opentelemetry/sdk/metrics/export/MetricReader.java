/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.util.EnumSet;
import javax.annotation.Nullable;

/**
 * A registered reader of metrics.
 *
 * <p>This interface provides the {@link io.opentelemetry.sdk.metrics.SdkMeterProvider} a mechanism
 * of global control over metrics during shutdown or memory pressure scenarios.
 */
public interface MetricReader {

  /** Return The set of all supported temporalities for this exporter. */
  EnumSet<AggregationTemporality> getSupportedTemporality();

  /** Return The preferred temporality for metrics. */
  @Nullable
  AggregationTemporality getPreferredTemporality();

  /**
   * Flushes metrics read by this reader.
   *
   * <p>In all scenarios, the associated {@link MetricProducer} should have its {@link
   * MetricProducer#collectAllMetrics()} method called.
   *
   * <p>For push endpoints, this should collect and report metrics as normal.
   *
   * @return the result of the shutdown.
   */
  CompletableResultCode flush();

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
