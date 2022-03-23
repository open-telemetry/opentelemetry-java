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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
public class AsynchronousMetricStorage<T, O> implements MetricStorage {
  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final ReentrantLock collectLock = new ReentrantLock();
  private final List<Consumer<O>> callbacks = new CopyOnWriteArrayList<>();
  private final MetricDescriptor metricDescriptor;
  private final TemporalMetricStorage<T> storage;
  private final AsyncAccumulator<T> accumulator;
  private final O measurement;

  private AsynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AsyncAccumulator<T> accumulator,
      O measurement) {
    this.metricDescriptor = metricDescriptor;
    this.storage = new TemporalMetricStorage<>(aggregator, /* isSynchronous= */ false);
    this.accumulator = accumulator;
    this.measurement = measurement;
  }

  /** Create an asynchronous storage instance for double measurements. */
  public static <T>
      AsynchronousMetricStorage<?, ObservableDoubleMeasurement> createDoubleAsyncStorage(
          RegisteredView registeredView, InstrumentDescriptor instrument) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrument);
    // TODO: optimize when aggregator is Aggregator.drop()
    Aggregator<T> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(instrument, ExemplarFilter.neverSample());
    AsyncAccumulator<T> accumulator = new AsyncAccumulator<>(instrument);
    ObservableDoubleMeasurement measurement =
        new ObservableDoubleMeasurementImpl<>(
            aggregator, accumulator, registeredView.getViewAttributesProcessor());
    return new AsynchronousMetricStorage<>(metricDescriptor, aggregator, accumulator, measurement);
  }

  /** Create an asynchronous storage instance for long measurements. */
  public static <T> AsynchronousMetricStorage<?, ObservableLongMeasurement> createLongAsyncStorage(
      RegisteredView registeredView, InstrumentDescriptor instrument) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrument);
    // TODO: optimize when aggregator is Aggregator.drop()
    Aggregator<T> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(instrument, ExemplarFilter.neverSample());
    AsyncAccumulator<T> accumulator = new AsyncAccumulator<>(instrument);
    ObservableLongMeasurement measurement =
        new ObservableLongMeasurementImpl<>(
            aggregator, accumulator, registeredView.getViewAttributesProcessor());
    return new AsynchronousMetricStorage<>(metricDescriptor, aggregator, accumulator, measurement);
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  @Override
  public MetricData collectAndReset(
      CollectionInfo collectionInfo,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    AggregationTemporality temporality =
        TemporalityUtils.resolveTemporality(collectionInfo.getPreferredAggregation());
    collectLock.lock();
    try {
      try {
        boolean empty = true;
        for (Consumer<O> callback : callbacks) {
          empty = false;
          callback.accept(measurement);
        }
        if (empty) {
          return EmptyMetricData.getInstance();
        }
      } catch (Throwable e) {
        propagateIfFatal(e);
        throttlingLogger.log(
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
          instrumentationScopeInfo,
          getMetricDescriptor(),
          temporality,
          accumulator.collectAndReset(),
          startEpochNanos,
          epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  /** Add a callback to the storage. */
  public void addCallback(Consumer<O> callback) {
    this.callbacks.add(callback);
  }

  /**
   * Remove the callback from the storage. Called when {@link AutoCloseable#close()} is invoked on
   * observable instruments.
   */
  public void removeCallback(Consumer<O> callback) {
    this.callbacks.remove(callback);
  }

  /** Helper class to record async measurements on demand. */
  // Visible for testing
  static final class AsyncAccumulator<T> {
    private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
    private final InstrumentDescriptor instrument;
    private Map<Attributes, T> currentAccumulation = new HashMap<>();

    AsyncAccumulator(InstrumentDescriptor instrument) {
      this.instrument = instrument;
    }

    void record(Attributes attributes, T accumulation) {
      // Check we're under the max allowed accumulations
      if (currentAccumulation.size() >= MetricStorageUtils.MAX_ACCUMULATIONS) {
        throttlingLogger.log(
            Level.WARNING,
            "Instrument "
                + instrument.getName()
                + " has exceeded the maximum allowed accumulations ("
                + MetricStorageUtils.MAX_ACCUMULATIONS
                + ").");
        return;
      }

      // Check there is not already a recording for the attributes
      if (currentAccumulation.containsKey(attributes)) {
        throttlingLogger.log(
            Level.WARNING,
            "Instrument "
                + instrument.getName()
                + " has recorded multiple values for the same attributes.");
        return;
      }

      currentAccumulation.put(attributes, accumulation);
    }

    Map<Attributes, T> collectAndReset() {
      Map<Attributes, T> result = currentAccumulation;
      currentAccumulation = new HashMap<>();
      return result;
    }
  }

  private static class ObservableLongMeasurementImpl<T> implements ObservableLongMeasurement {

    private final Aggregator<T> aggregator;
    private final AsyncAccumulator<T> asyncAccumulator;
    private final AttributesProcessor attributesProcessor;

    private ObservableLongMeasurementImpl(
        Aggregator<T> aggregator,
        AsyncAccumulator<T> asyncAccumulator,
        AttributesProcessor attributesProcessor) {
      this.aggregator = aggregator;
      this.asyncAccumulator = asyncAccumulator;
      this.attributesProcessor = attributesProcessor;
    }

    @Override
    public void record(long value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(long value, Attributes attributes) {
      T accumulation = aggregator.accumulateLongMeasurement(value, attributes, Context.current());
      if (accumulation != null) {
        asyncAccumulator.record(
            attributesProcessor.process(attributes, Context.current()), accumulation);
      }
    }
  }

  private static class ObservableDoubleMeasurementImpl<T> implements ObservableDoubleMeasurement {

    private final Aggregator<T> aggregator;
    private final AsyncAccumulator<T> asyncAccumulator;
    private final AttributesProcessor attributesProcessor;

    private ObservableDoubleMeasurementImpl(
        Aggregator<T> aggregator,
        AsyncAccumulator<T> asyncAccumulator,
        AttributesProcessor attributesProcessor) {
      this.aggregator = aggregator;
      this.asyncAccumulator = asyncAccumulator;
      this.attributesProcessor = attributesProcessor;
    }

    @Override
    public void record(double value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(double value, Attributes attributes) {
      T accumulation = aggregator.accumulateDoubleMeasurement(value, attributes, Context.current());
      if (accumulation != null) {
        asyncAccumulator.record(
            attributesProcessor.process(attributes, Context.current()), accumulation);
      }
    }
  }
}
