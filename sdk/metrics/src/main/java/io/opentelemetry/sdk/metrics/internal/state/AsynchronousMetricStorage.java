/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.internal.ThrowableUtil.propagateIfFatal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AsynchronousMetricStorage<T> implements MetricStorage {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DeltaMetricStorage.class.getName()));
  private final MetricDescriptor metricDescriptor;
  private final ReentrantLock collectLock = new ReentrantLock();
  private final AsyncAccumulator<T> asyncAccumulator;
  private final TemporalMetricStorage<T> storage;
  private final Runnable metricUpdater;

  /** Constructs asynchronous metric storage which stores nothing. */
  public static MetricStorage empty() {
    return EmptyMetricStorage.INSTANCE;
  }

  /** Constructs storage for {@code double} valued instruments. */
  public static <T> MetricStorage doubleAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation().createAggregator(instrument, ExemplarFilter.neverSample());

    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>(instrument);
    if (Aggregator.drop() == aggregator) {
      return empty();
    }
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableDoubleMeasurement result =
        new ObservableDoubleMeasurement() {
          @Override
          public void record(double value, Attributes attributes) {
            T accumulation =
                aggregator.accumulateDoubleMeasurement(value, attributes, Context.current());
            if (accumulation != null) {
              measurementAccumulator.record(
                  attributesProcessor.process(attributes, Context.current()), accumulation);
            }
          }

          @Override
          public void record(double value) {
            record(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage<>(
        metricDescriptor, aggregator, measurementAccumulator, () -> metricUpdater.accept(result));
  }

  /** Constructs storage for {@code long} valued instruments. */
  public static <T> MetricStorage longAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    Aggregator<T> aggregator =
        view.getAggregation().createAggregator(instrument, ExemplarFilter.neverSample());
    final AsyncAccumulator<T> measurementAccumulator = new AsyncAccumulator<>(instrument);
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableLongMeasurement result =
        new ObservableLongMeasurement() {

          @Override
          public void record(long value, Attributes attributes) {
            T accumulation =
                aggregator.accumulateLongMeasurement(value, attributes, Context.current());
            if (accumulation != null) {
              measurementAccumulator.record(
                  attributesProcessor.process(attributes, Context.current()), accumulation);
            }
          }

          @Override
          public void record(long value) {
            record(value, Attributes.empty());
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
  public MetricData collectAndReset(
      CollectionInfo collectionInfo,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    AggregationTemporality temporality =
        TemporalityUtils.resolveTemporality(collectionInfo.getPreferredAggregation());
    collectLock.lock();
    try {
      try {
        metricUpdater.run();
      } catch (Throwable e) {
        propagateIfFatal(e);
        logger.log(
            Level.WARNING,
            "An exception occurred invoking callback for instrument "
                + getMetricDescriptor().getName()
                + ".",
            e);
        return EmptyMetricData.getInstance();
      }
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
    private final InstrumentDescriptor instrument;
    private Map<Attributes, T> currentAccumulation = new HashMap<>();

    AsyncAccumulator(InstrumentDescriptor instrument) {
      this.instrument = instrument;
    }

    public void record(Attributes attributes, T accumulation) {
      if (currentAccumulation.size() >= MetricStorageUtils.MAX_ACCUMULATIONS) {
        logger.log(
            Level.WARNING,
            "Instrument "
                + instrument.getName()
                + " has exceeded the maximum allowed accumulations ("
                + MetricStorageUtils.MAX_ACCUMULATIONS
                + ").");
        return;
      }
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
