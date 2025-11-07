/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Records values from asynchronous instruments to associated {@link AsynchronousMetricStorage}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SdkObservableMeasurement
    implements ObservableLongMeasurement, ObservableDoubleMeasurement {

  private static final Logger logger = Logger.getLogger(SdkObservableMeasurement.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final InstrumentDescriptor instrumentDescriptor;
  private final List<AsynchronousMetricStorage<?>> storages;

  // These fields are set before invoking callbacks. They allow measurements to be recorded to the
  // storages for correct reader, and with the correct time.
  @Nullable private volatile RegisteredReader activeReader;

  private SdkObservableMeasurement(
      InstrumentationScopeInfo instrumentationScopeInfo,
      InstrumentDescriptor instrumentDescriptor,
      List<AsynchronousMetricStorage<?>> storages) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.instrumentDescriptor = instrumentDescriptor;
    this.storages = storages;
  }

  /**
   * Create a {@link SdkObservableMeasurement}.
   *
   * @param instrumentationScopeInfo the instrumentation scope info of corresponding meter
   * @param instrumentDescriptor the instrument descriptor
   * @param storages the storages to record to
   * @return the observable measurement
   */
  public static SdkObservableMeasurement create(
      InstrumentationScopeInfo instrumentationScopeInfo,
      InstrumentDescriptor instrumentDescriptor,
      List<AsynchronousMetricStorage<?>> storages) {
    return new SdkObservableMeasurement(instrumentationScopeInfo, instrumentDescriptor, storages);
  }

  /** Get the instrumentation scope info. */
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /**
   * Set the active reader, and clock information. {@link #unsetActiveReader()} MUST be called
   * after.
   */
  public void setActiveReader(
      RegisteredReader registeredReader, long startEpochNanos, long epochNanos) {
    this.activeReader = registeredReader;
    for (AsynchronousMetricStorage<?> storage : storages) {
      if (storage.getRegisteredReader().equals(activeReader)) {
        storage.setEpochInformation(startEpochNanos, epochNanos);
      }
    }
  }

  /**
   * Unset the active reader. Called after {@link #setActiveReader(RegisteredReader, long, long)}.
   */
  public void unsetActiveReader() {
    this.activeReader = null;
  }

  InstrumentDescriptor getInstrumentDescriptor() {
    return instrumentDescriptor;
  }

  List<AsynchronousMetricStorage<?>> getStorages() {
    return storages;
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  @Override
  public void record(long value, Attributes attributes) {
    RegisteredReader activeReader = this.activeReader;
    if (activeReader == null) {
      logNoActiveReader();
      return;
    }

    for (AsynchronousMetricStorage<?> storage : storages) {
      if (storage.getRegisteredReader().equals(activeReader)) {
        storage.record(attributes, value);
      }
    }
  }

  @Override
  public void record(double value) {
    record(value, Attributes.empty());
  }

  @Override
  public void record(double value, Attributes attributes) {
    RegisteredReader activeReader = this.activeReader;
    if (activeReader == null) {
      logNoActiveReader();
      return;
    }

    if (Double.isNaN(value)) {
      logger.log(
          Level.FINE,
          "Instrument "
              + instrumentDescriptor.getName()
              + " has recorded measurement Not-a-Number (NaN) value with attributes "
              + attributes
              + ". Dropping measurement.");
      return;
    }

    for (AsynchronousMetricStorage<?> storage : storages) {
      if (storage.getRegisteredReader().equals(activeReader)) {
        storage.record(attributes, value);
      }
    }
  }

  private void logNoActiveReader() {
    throttlingLogger.log(
        Level.FINE,
        "Measurement recorded for instrument "
            + instrumentDescriptor.getName()
            + " outside callback registered to instrument. Dropping measurement.");
  }
}
