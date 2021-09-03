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
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

// TODO(jsuereth): Error out any "delta" metrics on async instruments.

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AsynchronousMetricStorage implements MetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentProcessor<?> instrumentProcessor;
  private final Runnable metricUpdater;

  /** Constructs storage for {@code double} valued instruments. */
  public static <T> AsynchronousMetricStorage doubleAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    // TODO: Send metric descriptor to aggregator.
    Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrument)
            .create(resource, instrumentationLibraryInfo, instrument, metricDescriptor);
    final InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, startEpochNanos);
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableDoubleMeasurement result =
        new ObservableDoubleMeasurement() {
          @Override
          public void observe(double value, Attributes attributes) {
            instrumentProcessor.batch(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateDouble(value));
          }

          @Override
          public void observe(double value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage(
        metricDescriptor, instrumentProcessor, () -> metricUpdater.accept(result));
  }

  /** Constructs storage for {@code long} valued instruments. */
  public static <T> AsynchronousMetricStorage longAsynchronousAccumulator(
      View view,
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrument);
    // TODO: Send metric descriptor to aggregator.
    Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrument)
            .create(resource, instrumentationLibraryInfo, instrument, metricDescriptor);
    final InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, startEpochNanos);
    final AttributesProcessor attributesProcessor = view.getAttributesProcessor();
    // TODO: Find a way to grab the measurement JUST ONCE for all async metrics.
    final ObservableLongMeasurement result =
        new ObservableLongMeasurement() {

          @Override
          public void observe(long value, Attributes attributes) {
            instrumentProcessor.batch(
                attributesProcessor.process(attributes, Context.current()),
                aggregator.accumulateLong(value));
          }

          @Override
          public void observe(long value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage(
        metricDescriptor, instrumentProcessor, () -> metricUpdater.accept(result));
  }

  private AsynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      InstrumentProcessor<?> instrumentProcessor,
      Runnable metricUpdater) {
    this.metricDescriptor = metricDescriptor;
    this.instrumentProcessor = instrumentProcessor;
    this.metricUpdater = metricUpdater;
  }

  @Override
  public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      long startEpochNanos,
      long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.run();
      return instrumentProcessor.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
