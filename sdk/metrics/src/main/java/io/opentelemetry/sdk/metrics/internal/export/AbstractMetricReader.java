/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"AmbiguousMethodReference", "InconsistentOverloads"})
public abstract class AbstractMetricReader implements MetricReader {

  private static final MetricProducer EMPTY_METRIC_PRODUCER = Collections::emptyList;

  private final AtomicReference<MetricProducer> metricProducerRef = new AtomicReference<>();

  protected void registerMetricProducer(MetricProducer metricProducer) {
    if (!metricProducerRef.compareAndSet(null, metricProducer)) {
      throw new IllegalStateException(
          "registerMetricProducer(MetricProducer) called multiple times. Is the MetricReader associated with multiple SdkMeterProviders?");
    }
  }

  /** Call {@link #registerMetricProducer(MetricProducer)} on the reader. */
  public static void registerMetricProducer(
      MetricReader metricReader, MetricProducer metricProducer) {
    asAbstractMetricReader(metricReader).registerMetricProducer(metricProducer);
  }

  protected MetricProducer getMetricProducer() {
    MetricProducer metricProducer = metricProducerRef.get();
    return metricProducer == null ? EMPTY_METRIC_PRODUCER : metricProducer;
  }

  /**
   * Helper to return the {@code metricReader} as {@link AbstractMetricReader}.
   *
   * @throws IllegalArgumentException if {@code metricReader} is not an instance of {@link
   *     AbstractMetricReader}
   */
  public static AbstractMetricReader asAbstractMetricReader(MetricReader metricReader) {
    if (!(metricReader instanceof AbstractMetricReader)) {
      throw new IllegalArgumentException(
          "unrecognized MetricReader, custom MetricReader implementations are not currently supported");
    }
    return (AbstractMetricReader) metricReader;
  }
}
