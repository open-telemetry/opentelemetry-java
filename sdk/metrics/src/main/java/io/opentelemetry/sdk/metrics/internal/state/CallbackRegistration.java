/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.internal.ThrowableUtil.propagateIfFatal;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A registered callback.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class CallbackRegistration {
  private static final Logger logger = Logger.getLogger(CallbackRegistration.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final List<SdkObservableMeasurement> observableMeasurements;
  private final Runnable callback;
  private final String callbackDescription;
  private final boolean hasStorages;

  private CallbackRegistration(
      List<SdkObservableMeasurement> observableMeasurements, Runnable callback) {
    this.observableMeasurements = observableMeasurements;
    this.callback = callback;
    List<InstrumentDescriptor> instrumentDescriptors =
        observableMeasurements.stream()
            .map(SdkObservableMeasurement::getInstrumentDescriptor)
            .collect(toList());
    this.callbackDescription = callbackDescription(instrumentDescriptors);
    this.hasStorages =
        observableMeasurements.stream()
            .flatMap(measurement -> measurement.getStorages().stream())
            .findAny()
            .isPresent();
  }

  /**
   * Create a callback registration.
   *
   * <p>The {@code observableMeasurements} define the set of measurements the {@code runnable} may
   * record to. The active reader of each {@code observableMeasurements} is set via {@link
   * SdkObservableMeasurement#setActiveReader(RegisteredReader)} before {@code runnable} is called,
   * and set to {@code null} afterwards.
   *
   * @param observableMeasurements the measurements that the runnable may record to
   * @param runnable the callback
   * @return the callback registration
   */
  public static CallbackRegistration create(
      List<SdkObservableMeasurement> observableMeasurements, Runnable runnable) {
    return new CallbackRegistration(observableMeasurements, runnable);
  }

  // Visible for test
  static String callbackDescription(List<InstrumentDescriptor> instrumentDescriptors) {
    if (instrumentDescriptors.size() == 0) {
      throw new IllegalStateException("Callback with no instruments is not allowed");
    }
    if (instrumentDescriptors.size() == 1) {
      return "Instrument " + instrumentDescriptors.get(0).getName();
    }
    StringBuilder description = new StringBuilder("BatchCallback(");
    description.append(
        instrumentDescriptors.stream()
            .map(InstrumentDescriptor::getName)
            .collect(Collectors.joining(",", "[", "]")));
    return description.append(")").toString();
  }

  public String getCallbackDescription() {
    return callbackDescription;
  }

  void invokeCallback(RegisteredReader reader) {
    // Return early if no storages are registered
    if (!hasStorages) {
      return;
    }
    // Set the active reader on each observable measurement so that measurements are only recorded
    // to relevant storages
    observableMeasurements.forEach(
        observableMeasurement -> observableMeasurement.setActiveReader(reader));
    try {
      callback.run();
    } catch (Throwable e) {
      propagateIfFatal(e);
      throttlingLogger.log(
          Level.WARNING,
          "An exception occurred invoking callback for " + callbackDescription + ".",
          e);
    } finally {
      observableMeasurements.forEach(
          observableMeasurement -> observableMeasurement.setActiveReader(null));
    }
  }
}
