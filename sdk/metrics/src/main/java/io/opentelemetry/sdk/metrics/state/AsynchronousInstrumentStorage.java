/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.DoubleMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.instrument.LongMeasurement;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class AsynchronousInstrumentStorage implements InstrumentStorage {
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentDescriptor instrument;
  private final Consumer<Aggregator<?>> metricUpdater;
  private final Aggregator<?> aggregator;

  static <T> AsynchronousInstrumentStorage create(
      InstrumentDescriptor instrument,
      Consumer<? extends ObservableMeasurement> callback,
      Aggregator<T> aggregator) {
    return new AsynchronousInstrumentStorage(instrument, wrapCallback(callback), aggregator);
  }

  /** Type gymnastics to handle both long/double observable measurements. */
  @SuppressWarnings("unchecked")
  private static <T extends ObservableMeasurement> Consumer<Aggregator<?>> wrapCallback(
      Consumer<T> callback) {
    return (storage) -> callback.accept((T) new MyObservableMeasurement(storage));
  }

  private AsynchronousInstrumentStorage(
      InstrumentDescriptor instrument,
      Consumer<Aggregator<?>> metricUpdater,
      Aggregator<?> aggregator) {
    this.instrument = instrument;
    this.metricUpdater = metricUpdater;
    this.aggregator = aggregator;
  }

  @Override
  public InstrumentDescriptor getDescriptor() {
    return instrument;
  }

  @Override
  public List<MetricData> collectAndReset(long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.accept(aggregator);
      return aggregator.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  /** Converts from observable callbacks to measurements. */
  static class MyObservableMeasurement
      implements ObservableLongMeasurement, ObservableDoubleMeasurement {
    private final Aggregator<?> storage;

    MyObservableMeasurement(Aggregator<?> storage) {
      this.storage = storage;
    }

    @Override
    public void observe(double value, Attributes attributes) {
      storage.batchRecord(DoubleMeasurement.createNoContext(value, attributes));
    }

    @Override
    public void observe(double value) {
      observe(value, Attributes.empty());
    }

    @Override
    public void observe(long value, Attributes attributes) {
      storage.batchRecord(LongMeasurement.createNoContext(value, attributes));
    }

    @Override
    public void observe(long value) {
      observe(value, Attributes.empty());
    }
  }
}
