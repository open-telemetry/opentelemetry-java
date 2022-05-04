/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.internal.ThrowableUtil.propagateIfFatal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A registered callback of an asynchronous instrument.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class CallbackRegistration<T> {
  private static final Logger logger = Logger.getLogger(CallbackRegistration.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final InstrumentDescriptor instrumentDescriptor;
  private final Consumer<T> callback;
  private final T measurement;
  private final List<AsynchronousMetricStorage<?, ?>> storages;
  @Nullable private volatile RegisteredReader activeReader;

  private CallbackRegistration(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<T> callback,
      List<AsynchronousMetricStorage<?, ?>> storages,
      Function<CallbackRegistration<T>, T> measurementProvider) {
    this.instrumentDescriptor = instrumentDescriptor;
    this.callback = callback;
    this.measurement = measurementProvider.apply(this);
    this.storages = storages;
  }

  /** Create a {@link CallbackRegistration} for a {@code double} asynchronous instrument. */
  public static CallbackRegistration<ObservableDoubleMeasurement> createDouble(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<ObservableDoubleMeasurement> callback,
      List<AsynchronousMetricStorage<?, ?>> asyncMetricStorages) {
    return new CallbackRegistration<>(
        instrumentDescriptor,
        callback,
        asyncMetricStorages,
        callbackRegistration -> callbackRegistration.new ObservableDoubleMeasurementImpl());
  }

  /** Create a {@link CallbackRegistration} for a {@code long} asynchronous instrument. */
  public static CallbackRegistration<ObservableLongMeasurement> createLong(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<ObservableLongMeasurement> callback,
      List<AsynchronousMetricStorage<?, ?>> asyncMetricStorages) {
    return new CallbackRegistration<>(
        instrumentDescriptor,
        callback,
        asyncMetricStorages,
        callbackRegistration -> callbackRegistration.new ObservableLongMeasurementImpl());
  }

  public InstrumentDescriptor getInstrumentDescriptor() {
    return instrumentDescriptor;
  }

  void invokeCallback(RegisteredReader reader) {
    // Return early if no storages are registered
    if (storages.isEmpty()) {
      return;
    }
    try {
      // Set the active reader so that measurements are only recorded to relevant storages
      activeReader = reader;
      callback.accept(measurement);
    } catch (Throwable e) {
      propagateIfFatal(e);
      throttlingLogger.log(
          Level.WARNING,
          "An exception occurred invoking callback for instrument "
              + instrumentDescriptor.getName()
              + ".",
          e);
    } finally {
      activeReader = null;
    }
  }

  private class ObservableDoubleMeasurementImpl implements ObservableDoubleMeasurement {

    @Override
    public void record(double value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(double value, Attributes attributes) {
      for (AsynchronousMetricStorage<?, ?> asyncMetricStorage : storages) {
        if (asyncMetricStorage.getRegisteredReader().equals(activeReader)) {
          asyncMetricStorage.recordDouble(value, attributes);
        }
      }
    }
  }

  private class ObservableLongMeasurementImpl implements ObservableLongMeasurement {

    @Override
    public void record(long value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(long value, Attributes attributes) {
      for (AsynchronousMetricStorage<?, ?> asyncMetricStorage : storages) {
        if (asyncMetricStorage.getRegisteredReader().equals(activeReader)) {
          asyncMetricStorage.recordLong(value, attributes);
        }
      }
    }
  }
}
