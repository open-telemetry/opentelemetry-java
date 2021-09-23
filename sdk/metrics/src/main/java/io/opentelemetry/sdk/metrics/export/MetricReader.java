/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;

/**
 * A registered reader of metrics.
 *
 * <p>This interface provides the {@link io.opentelemetry.sdk.metrics.SdkMeterProvider} a mechanism
 * of global control over metrics during shutdown or memory pressure scenarios.
 */
public interface MetricReader {

  /**
   * Flushes metrics read by this reader.
   *
   * <p>In all scenarios, the associated {@link MetricProducer} should have its {@link
   * MetricProducer#collectAllMetrics()} method called.
   *
   * <p>For push endpoints, this should collect and report metrics as normal.
   *
   * <p>For pull endpoints, this may be called due to memory pressure or during shutdown of the
   * application. Delta metrics collected in this way may be dropped, and the {@link MetricReader}
   * is expected to handle this scenario.
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
