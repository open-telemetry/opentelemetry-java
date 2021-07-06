/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.CollectionHandle;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.DoubleMeasurement;
import io.opentelemetry.sdk.metrics.instrument.LongMeasurement;
import io.opentelemetry.sdk.metrics.view.AttributesProcessor;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AsynchronousInstrumentStorage implements InstrumentStorage {
  private final ReentrantLock collectLock = new ReentrantLock();
  private final BiConsumer<Aggregator<?>, AttributesProcessor> metricUpdater;
  private final Aggregator<?> aggregator;
  private final AttributesProcessor attributesProcessor;

  static <T> AsynchronousInstrumentStorage create(
      Consumer<? extends ObservableMeasurement> callback,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor) {
    return new AsynchronousInstrumentStorage(
        wrapCallback(callback), aggregator, attributesProcessor);
  }

  /** Type gymnastics to handle both long/double observable measurements. */
  @SuppressWarnings("unchecked")
  private static <T extends ObservableMeasurement>
      BiConsumer<Aggregator<?>, AttributesProcessor> wrapCallback(Consumer<T> callback) {
    return (storage, processor) ->
        callback.accept((T) new MyObservableMeasurement(storage, processor));
  }

  private AsynchronousInstrumentStorage(
      BiConsumer<Aggregator<?>, AttributesProcessor> metricUpdater,
      Aggregator<?> aggregator,
      AttributesProcessor attributesProcessor) {
    this.metricUpdater = metricUpdater;
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
  }

  @Override
  public List<MetricData> collectAndReset(
      CollectionHandle collector, Set<CollectionHandle> allCollectors, long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.accept(aggregator, attributesProcessor);
      return aggregator.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  /** Converts from observable callbacks to measurements. */
  static class MyObservableMeasurement
      implements ObservableLongMeasurement, ObservableDoubleMeasurement {
    private final Aggregator<?> storage;
    private final AttributesProcessor attributesProcessor;

    MyObservableMeasurement(Aggregator<?> storage, AttributesProcessor attributesProcessor) {
      this.storage = storage;
      this.attributesProcessor = attributesProcessor;
    }

    @Override
    public void observe(double value, Attributes attributes) {
      final Attributes realAttributes = attributesProcessor.process(attributes, Context.current());
      storage.batchRecord(DoubleMeasurement.createNoContext(value, realAttributes));
    }

    @Override
    public void observe(double value) {
      observe(value, Attributes.empty());
    }

    @Override
    public void observe(long value, Attributes attributes) {
      final Attributes realAttributes = attributesProcessor.process(attributes, Context.current());
      storage.batchRecord(LongMeasurement.createNoContext(value, realAttributes));
    }

    @Override
    public void observe(long value) {
      observe(value, Attributes.empty());
    }
  }
}
