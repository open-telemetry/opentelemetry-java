/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.internal.ThrowableUtil.propagateIfFatal;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
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
  private final String callbackDescription;
  private final List<AsynchronousMetricStorage<?>> storages;
  private final Runnable callback;

  private CallbackRegistration(
      List<InstrumentDescriptor> instrumentDescriptors,
      List<AsynchronousMetricStorage<?>> storages,
      Runnable callback) {
    this.callbackDescription = callbackDescription(instrumentDescriptors);
    this.storages = storages;
    this.callback = callback;
  }

  /**
   * Create a callback registration.
   *
   * <p>The {@code observableMeasurements} define the set of measurements the {@code runnable} may
   * record to. The {@link AsynchronousMetricStorage}s for each is {@link
   * AsynchronousMetricStorage#unlock()}ed before {@code runnable} is called, and {@link
   * AsynchronousMetricStorage#lock()}ed afterwards.
   *
   * @param observableMeasurements the measurements that the runnable may record to
   * @param runnable the callback
   * @return the callback registration
   */
  public static CallbackRegistration create(
      List<SdkObservableMeasurement> observableMeasurements, Runnable runnable) {
    List<InstrumentDescriptor> instrumentDescriptors =
        observableMeasurements.stream()
            .map(SdkObservableMeasurement::getInstrumentDescriptor)
            .collect(toList());
    List<AsynchronousMetricStorage<?>> storages =
        observableMeasurements.stream()
            .flatMap(measurement -> measurement.getStorages().stream())
            .collect(toList());
    return new CallbackRegistration(instrumentDescriptors, storages, runnable);
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

  void invokeCallback() {
    // Return early if no storages are registered
    if (storages.size() == 0) {
      return;
    }
    storages.forEach(AsynchronousMetricStorage::unlock);
    try {
      callback.run();
    } catch (Throwable e) {
      propagateIfFatal(e);
      throttlingLogger.log(
          Level.WARNING,
          "An exception occurred invoking callback for " + callbackDescription + ".",
          e);
    } finally {
      storages.forEach(AsynchronousMetricStorage::lock);
    }
  }
}
