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
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;
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

  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  /** Constructs storage for {@code double} valued instruments. */
  public static <T> AsynchronousMetricStorage<T> doubleAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrument)
            .create(
                resource,
                instrumentationLibraryInfo,
                instrument,
                metricDescriptor,
                ExemplarReservoir::noSamples);

    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>();
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableDoubleMeasurement result =
        new ObservableDoubleMeasurement() {
          @Override
          public void observe(double value, Attributes attributes) {
            measurementAccumulator.record(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateDouble(value));
          }

          @Override
          public void observe(double value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage<>(
        metricDescriptor, aggregator, measurementAccumulator, () -> metricUpdater.accept(result));
  }

  /** Constructs storage for {@code long} valued instruments. */
  public static <T> AsynchronousMetricStorage<T> longAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrument)
            .create(
                resource,
                instrumentationLibraryInfo,
                instrument,
                metricDescriptor,
                ExemplarReservoir::noSamples);
    if (aggregator.isStateful()) {
      // The aggregator is expecting to diff SUMs for DELTA temporality.
      logger.warning(
          String.format(
              "Unable to provide DELTA accumulation on %s for instrument: %s",
              metricDescriptor, instrument));
    }
    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>();
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableLongMeasurement result =
        new ObservableLongMeasurement() {

          @Override
          public void observe(long value, Attributes attributes) {
            measurementAccumulator.record(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateLong(value));
          }

          @Override
          public void observe(long value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage<>(
        metricDescriptor, aggregator, measurementAccumulator, () -> metricUpdater.accept(result));
  }

  private AsynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AsyncAccumulator<T> asyncAccumulator,
      Runnable metricUpdater) {
    this.metricDescriptor = metricDescriptor;
    this.asyncAccumulator = asyncAccumulator;
    this.metricUpdater = metricUpdater;
    this.storage = new TemporalMetricStorage<>(aggregator, /* isSynchronous= */ false);
  }

  @Override
  @Nullable
  public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      long startEpochNanos,
      long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.run();
      return storage.buildMetricFor(
          collector, asyncAccumulator.collectAndReset(), startEpochNanos, epochNanos);
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
