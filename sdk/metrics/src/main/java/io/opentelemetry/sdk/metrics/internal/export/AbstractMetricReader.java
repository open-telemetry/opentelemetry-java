/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Abstract superclass for {@link MetricReader} implementations. {@link
 * SdkMeterProviderBuilder#registerMetricReader(MetricReader)} expects {@link MetricReader}
 * implementations to extends from this class.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class AbstractMetricReader implements MetricReader {

  private static final MetricProducer EMPTY_METRIC_PRODUCER = Collections::emptyList;

  private final AtomicReference<MetricProducer> metricProducerRef = new AtomicReference<>();
  private final Function<InstrumentType, AggregationTemporality> aggregationTemporalityFunction;

  protected AbstractMetricReader(
      Function<InstrumentType, AggregationTemporality> aggregationTemporalityFunction) {
    this.aggregationTemporalityFunction = aggregationTemporalityFunction;
  }

  /**
   * Called exactly once by {@link SdkMeterProvider} during initialization to register the {@link
   * MetricProducer}.
   */
  protected void registerMetricProducer(MetricProducer metricProducer) {
    if (!metricProducerRef.compareAndSet(null, metricProducer)) {
      throw new IllegalStateException(
          "registerMetricProducer(MetricProducer) called multiple times. Is the MetricReader associated with multiple SdkMeterProviders?");
    }
  }

  /**
   * Cast the reader to {@link AbstractMetricReader} and call {@link
   * #registerMetricProducer(MetricProducer)}.
   */
  public static void registerMetricProducer(
      MetricProducer metricProducer, MetricReader metricReader) {
    asAbstractMetricReader(metricReader).registerMetricProducer(metricProducer);
  }

  /**
   * Get the {@link MetricProducer} registered by {@link SdkMeterProvider}.
   *
   * @return the registered producer, or a producer that returns empty if {@link
   *     #registerMetricProducer(MetricProducer)} has not yet been called
   */
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

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporalityFunction.apply(instrumentType);
  }
}
