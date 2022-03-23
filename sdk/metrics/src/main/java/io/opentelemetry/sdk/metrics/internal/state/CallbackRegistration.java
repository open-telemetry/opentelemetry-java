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
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  private final boolean noStoragesRegistered;

  private CallbackRegistration(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<T> callback,
      T measurement,
      List<AsynchronousMetricStorage<?>> storages) {
    this.instrumentDescriptor = instrumentDescriptor;
    this.callback = callback;
    this.measurement = measurement;
    this.noStoragesRegistered = storages.size() == 0;
  }

  /** Create a {@link CallbackRegistration} for a {@code double} asynchronous instrument. */
  public static CallbackRegistration<ObservableDoubleMeasurement> createDouble(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<ObservableDoubleMeasurement> callback,
      List<AsynchronousMetricStorage<?>> asyncMetricStorages) {
    ObservableDoubleMeasurement measurement =
        new ObservableDoubleMeasurementImpl(asyncMetricStorages);
    return new CallbackRegistration<>(
        instrumentDescriptor, callback, measurement, asyncMetricStorages);
  }

  /** Create a {@link CallbackRegistration} for a {@code long} asynchronous instrument. */
  public static CallbackRegistration<ObservableLongMeasurement> createLong(
      InstrumentDescriptor instrumentDescriptor,
      Consumer<ObservableLongMeasurement> callback,
      List<AsynchronousMetricStorage<?>> asyncMetricStorages) {
    ObservableLongMeasurement measurement = new ObservableLongMeasurementImpl(asyncMetricStorages);
    return new CallbackRegistration<>(
        instrumentDescriptor, callback, measurement, asyncMetricStorages);
  }

  public InstrumentDescriptor getInstrumentDescriptor() {
    return instrumentDescriptor;
  }

  void invokeCallback() {
    // Return early if no storages are registered
    if (noStoragesRegistered) {
      return;
    }
    try {
      callback.accept(measurement);
    } catch (Throwable e) {
      propagateIfFatal(e);
      throttlingLogger.log(
          Level.WARNING,
          "An exception occurred invoking callback for instrument "
              + instrumentDescriptor.getName()
              + ".",
          e);
    }
  }

  private static class ObservableDoubleMeasurementImpl implements ObservableDoubleMeasurement {

    private final List<AsynchronousMetricStorage<?>> asyncMetricStorages;

    private ObservableDoubleMeasurementImpl(
        List<AsynchronousMetricStorage<?>> asyncMetricStorages) {
      this.asyncMetricStorages = asyncMetricStorages;
    }

    @Override
    public void record(double value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(double value, Attributes attributes) {
      for (AsynchronousMetricStorage<?> asyncMetricStorage : asyncMetricStorages) {
        asyncMetricStorage.recordDouble(value, attributes);
      }
    }
  }

  private static class ObservableLongMeasurementImpl implements ObservableLongMeasurement {

    private final List<AsynchronousMetricStorage<?>> asyncMetricStorages;

    private ObservableLongMeasurementImpl(List<AsynchronousMetricStorage<?>> asyncMetricStorages) {
      this.asyncMetricStorages = asyncMetricStorages;
    }

    @Override
    public void record(long value) {
      record(value, Attributes.empty());
    }

    @Override
    public void record(long value, Attributes attributes) {
      for (AsynchronousMetricStorage<?> asyncMetricStorage : asyncMetricStorages) {
        asyncMetricStorage.recordLong(value, attributes);
      }
    }
  }
}
