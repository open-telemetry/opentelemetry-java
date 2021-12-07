/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingMetricExporter implements MetricExporter {
  private static final Logger logger = Logger.getLogger(LoggingMetricExporter.class.getName());

  private final AggregationTemporality preferredTemporality;

  /**
   * Returns a new {@link LoggingMetricExporter} with a preferred temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static LoggingMetricExporter create() {
    return create(AggregationTemporality.CUMULATIVE);
  }

  /** Returns a new {@link LoggingMetricExporter} with the given {@code preferredTemporality}. */
  public static LoggingMetricExporter create(AggregationTemporality preferredTemporality) {
    return new LoggingMetricExporter(preferredTemporality);
  }

  /**
   * Class constructor with a preferred temporality of {@link AggregationTemporality#CUMULATIVE}.
   *
   * @deprecated Use {@link #create()}.
   */
  @Deprecated
  public LoggingMetricExporter() {
    this(AggregationTemporality.CUMULATIVE);
  }

  private LoggingMetricExporter(AggregationTemporality preferredTemporality) {
    this.preferredTemporality = preferredTemporality;
  }

  @Override
  public AggregationTemporality getPreferredTemporality() {
    return preferredTemporality;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    logger.info("Received a collection of " + metrics.size() + " metrics for export.");
    for (MetricData metricData : metrics) {
      logger.log(Level.INFO, "metric: {0}", metricData);
    }
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Flushes the data.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    CompletableResultCode resultCode = new CompletableResultCode();
    for (Handler handler : logger.getHandlers()) {
      try {
        handler.flush();
      } catch (Throwable t) {
        return resultCode.fail();
      }
    }
    return resultCode.succeed();
  }

  @Override
  public CompletableResultCode shutdown() {
    // no-op
    this.flush();
    return CompletableResultCode.ofSuccess();
  }
}
