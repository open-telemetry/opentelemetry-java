/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static java.util.logging.Level.WARNING;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Helper to make implementing builders easier. */
abstract class AbstractInstrumentBuilder<BuilderT extends AbstractInstrumentBuilder<?>> {

  static final String DEFAULT_UNIT = "";
  public static final String LOGGER_NAME = "io.opentelemetry.sdk.metrics.AbstractInstrumentBuilder";
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private final MeterProviderSharedState meterProviderSharedState;
  private final InstrumentType type;
  private final InstrumentValueType valueType;
  private String description;
  private String unit;
  private Advice advice;

  protected final MeterSharedState meterSharedState;
  protected final String instrumentName;

  AbstractInstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentType type,
      InstrumentValueType valueType,
      String name,
      String description,
      String unit) {
    this(
        meterProviderSharedState,
        meterSharedState,
        type,
        valueType,
        name,
        description,
        unit,
        Advice.empty());
  }

  AbstractInstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentType type,
      InstrumentValueType valueType,
      String name,
      String description,
      String unit,
      Advice advice) {
    this.type = type;
    this.valueType = valueType;
    this.instrumentName = name;
    this.description = description;
    this.unit = unit;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
    this.advice = advice;
  }

  protected abstract BuilderT getThis();

  public BuilderT setUnit(String unit) {
    if (!checkValidInstrumentUnit(
        unit,
        " Using \"" + DEFAULT_UNIT + "\" for instrument " + this.instrumentName + " instead.")) {
      this.unit = DEFAULT_UNIT;
    } else {
      this.unit = unit;
    }
    return getThis();
  }

  public BuilderT setDescription(String description) {
    this.description = description;
    return getThis();
  }

  protected <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState, meterSharedState, instrumentName, description, unit, advice);
  }

  protected void setAdvice(Advice advice) {
    this.advice = advice;
  }

  final <I extends AbstractInstrument> I buildSynchronousInstrument(
      BiFunction<InstrumentDescriptor, WriteableMetricStorage, I> instrumentFactory) {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(instrumentName, description, unit, type, valueType, advice);
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.apply(descriptor, storage);
  }

  final SdkObservableInstrument registerDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  final SdkObservableInstrument registerLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  final SdkObservableMeasurement buildObservableMeasurement(InstrumentType type) {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(instrumentName, description, unit, type, valueType, advice);
    return meterSharedState.registerObservableMeasurement(descriptor);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{descriptor="
        + InstrumentDescriptor.create(instrumentName, description, unit, type, valueType, advice)
        + "}";
  }

  /** Check if the instrument unit is valid. If invalid, log a warning. */
  static boolean checkValidInstrumentUnit(String unit) {
    return checkValidInstrumentUnit(unit, "");
  }

  /**
   * Check if the instrument unit is valid. If invalid, log a warning with the {@code logSuffix}
   * appended. This method should be removed and unit validation should not happen.
   */
  static boolean checkValidInstrumentUnit(String unit, String logSuffix) {
    if (unit != null
        && !unit.equals("")
        && unit.length() < 64
        && StandardCharsets.US_ASCII.newEncoder().canEncode(unit)) {
      return true;
    }
    if (LOGGER.isLoggable(WARNING)) {
      LOGGER.log(
          WARNING,
          "Unit \""
              + unit
              + "\" is invalid. Instrument unit must be 63 or fewer ASCII characters."
              + logSuffix,
          new AssertionError());
    }
    return false;
  }

  @FunctionalInterface
  protected interface SwapBuilder<T> {
    T newBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name,
        String description,
        String unit,
        Advice advice);
  }
}
