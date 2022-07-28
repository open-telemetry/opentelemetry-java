/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class AsynchronousMetricStorage<T, U extends ExemplarData> implements MetricStorage {
  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final TemporalMetricStorage<T, U> metricStorage;
  private final Aggregator<T, U> aggregator;
  private final AttributesProcessor attributesProcessor;
  private Map<Attributes, T> accumulations = new HashMap<>();
  private final int maxAccumulations;

  private AsynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T, U> aggregator,
      AttributesProcessor attributesProcessor,
      int maxAccumulations) {
    this.registeredReader = registeredReader;
    this.metricDescriptor = metricDescriptor;
    AggregationTemporality aggregationTemporality =
        registeredReader
            .getReader()
            .getAggregationTemporality(metricDescriptor.getSourceInstrument().getType());
    this.metricStorage =
        new TemporalMetricStorage<>(
            aggregator,
            /* isSynchronous= */ false,
            registeredReader,
            aggregationTemporality,
            metricDescriptor);
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.maxAccumulations = maxAccumulations;
  }

  /**
   * Create an asynchronous storage instance for the {@link View} and {@link InstrumentDescriptor}.
   */
  // TODO(anuraaga): The cast to generic type here looks suspicious.
  static <T, U extends ExemplarData> AsynchronousMetricStorage<T, U> create(
      RegisteredReader registeredReader,
      RegisteredView registeredView,
      InstrumentDescriptor instrumentDescriptor,
      int maxAccumulations) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrumentDescriptor);
    Aggregator<T, U> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(instrumentDescriptor, ExemplarFilter.neverSample());
    return new AsynchronousMetricStorage<>(
        registeredReader,
        metricDescriptor,
        aggregator,
        registeredView.getViewAttributesProcessor(),
        maxAccumulations);
  }

  /** Record callback long measurements from {@link ObservableLongMeasurement}. */
  void recordLong(long value, Attributes attributes) {
    T accumulation = aggregator.accumulateLongMeasurement(value, attributes, Context.current());
    if (accumulation != null) {
      recordAccumulation(accumulation, attributes);
    }
  }

  /** Record callback double measurements from {@link ObservableDoubleMeasurement}. */
  void recordDouble(double value, Attributes attributes) {
    T accumulation = aggregator.accumulateDoubleMeasurement(value, attributes, Context.current());
    if (accumulation != null) {
      recordAccumulation(accumulation, attributes);
    }
  }

  private void recordAccumulation(T accumulation, Attributes attributes) {
    Attributes processedAttributes = attributesProcessor.process(attributes, Context.current());

    if (accumulations.size() >= maxAccumulations) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed accumulations ("
              + maxAccumulations
              + ").");
      return;
    }

    // Check there is not already a recording for the attributes
    if (accumulations.containsKey(attributes)) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded multiple values for the same attributes.");
      return;
    }

    accumulations.put(processedAttributes, accumulation);
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  @Override
  public RegisteredReader getRegisteredReader() {
    return registeredReader;
  }

  @Override
  public MetricData collectAndReset(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    Map<Attributes, T> currentAccumulations = accumulations;
    accumulations = new HashMap<>();
    return metricStorage.buildMetricFor(
        resource, instrumentationScopeInfo, currentAccumulations, startEpochNanos, epochNanos);
  }

  @Override
  public boolean isEmpty() {
    return aggregator == Aggregator.drop();
  }
}
