/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AsynchronousMetricStorage<T> implements MetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final ReentrantLock collectLock = new ReentrantLock();
  private final AsyncAccumulator<T> asyncAccumulator;
  private final TemporalMetricStorage<T> storage;
  private final Runnable metricUpdater;
  @Nullable private final AggregationTemporality configuredTemporality;

  /** Constructs asynchronous metric storage which stores nothing. */
  public static MetricStorage empty() {
    return EmptyMetricStorage.INSTANCE;
  }

  /** Constructs storage for {@code double} valued instruments. */
  @Nullable
  public static <T> MetricStorage doubleAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation().createAggregator(instrument, ExemplarFilter.neverSample());

    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>();
    if (Aggregator.empty() == aggregator) {
      return empty();
    }
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableDoubleMeasurement result =
        new ObservableDoubleMeasurement() {
          @Override
          public void observe(double value, Attributes attributes) {
            measurementAccumulator.record(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateDoubleMeasurement(value, attributes, Context.current()));
          }

          @Override
          public void observe(double value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage<>(
        metricDescriptor,
        aggregator,
        measurementAccumulator,
        () -> metricUpdater.accept(result),
        view.getAggregation().getConfiguredTemporality());
  }

  /** Constructs storage for {@code long} valued instruments. */
  public static <T> MetricStorage longAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation().createAggregator(instrument, ExemplarFilter.neverSample());
    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>();
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableLongMeasurement result =
        new ObservableLongMeasurement() {

          @Override
          public void observe(long value, Attributes attributes) {
            measurementAccumulator.record(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateLongMeasurement(value, attributes, Context.current()));
          }

          @Override
          public void observe(long value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage<>(
        metricDescriptor,
        aggregator,
        measurementAccumulator,
        () -> metricUpdater.accept(result),
        view.getAggregation().getConfiguredTemporality());
  }

  private AsynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AsyncAccumulator<T> asyncAccumulator,
      Runnable metricUpdater,
      @Nullable AggregationTemporality configuredTemporality) {
    this.metricDescriptor = metricDescriptor;
    this.asyncAccumulator = asyncAccumulator;
    this.metricUpdater = metricUpdater;
    this.storage = new TemporalMetricStorage<>(aggregator, /* isSynchronous= */ false);
    this.configuredTemporality = configuredTemporality;
  }

  @Override
  @Nullable
  public MetricData collectAndReset(
      CollectionInfo collectionInfo,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    AggregationTemporality temporality =
        TemporalityUtils.resolveTemporality(
            collectionInfo.getSupportedAggregation(),
            collectionInfo.getPreferredAggregation(),
            configuredTemporality);
    collectLock.lock();
    try {
      metricUpdater.run();
      return storage.buildMetricFor(
          collectionInfo.getCollector(),
          resource,
          instrumentationLibraryInfo,
          getMetricDescriptor(),
          temporality,
          asyncAccumulator.collectAndReset(),
          startEpochNanos,
          epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  /** Helper class to record async measurements on demand. */
  private static final class AsyncAccumulator<T> {
    private Map<Attributes, T> currentAccumulation = new HashMap<>();

    public void record(Attributes attributes, T accumulation) {
      // TODO: error on metric overwrites
      currentAccumulation.put(attributes, accumulation);
    }

    public Map<Attributes, T> collectAndReset() {
      Map<Attributes, T> result = currentAccumulation;
      currentAccumulation = new HashMap<>();
      return result;
    }
  }
}
