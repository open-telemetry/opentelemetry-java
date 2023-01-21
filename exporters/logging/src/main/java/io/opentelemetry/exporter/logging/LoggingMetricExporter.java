/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingMetricExporter implements MetricExporter {
  private static final Logger logger = Logger.getLogger(LoggingMetricExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();
  private final AggregationTemporality aggregationTemporality;

  /**
   * Returns a new {@link LoggingMetricExporter} with an aggregation temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static LoggingMetricExporter create() {
    return create(AggregationTemporality.CUMULATIVE);
  }

  /** Returns a new {@link LoggingMetricExporter} with the given {@code aggregationTemporality}. */
  public static LoggingMetricExporter create(AggregationTemporality aggregationTemporality) {
    return new LoggingMetricExporter(aggregationTemporality);
  }

  /**
   * Class constructor with an aggregation temporality of {@link AggregationTemporality#CUMULATIVE}.
   *
   * @deprecated Use {@link #create()}.
   */
  @Deprecated
  public LoggingMetricExporter() {
    this(AggregationTemporality.CUMULATIVE);
  }

  private LoggingMetricExporter(AggregationTemporality aggregationTemporality) {
    this.aggregationTemporality = aggregationTemporality;
  }

  /**
   * Return the aggregation temporality.
   *
   * @deprecated Use {@link #getAggregationTemporality(InstrumentType)}.
   */
  @Deprecated
  public AggregationTemporality getPreferredTemporality() {
    return aggregationTemporality;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

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
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return flush();
  }
}
