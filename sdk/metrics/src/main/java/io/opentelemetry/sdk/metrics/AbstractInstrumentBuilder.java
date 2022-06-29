/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.internal.ValidationUtil;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
abstract class AbstractInstrumentBuilder<BuilderT extends AbstractInstrumentBuilder<?>> {

  static final String DEFAULT_UNIT = "";

  private final MeterProviderSharedState meterProviderSharedState;
  private String description;
  private String unit;

  protected final MeterSharedState meterSharedState;
  protected final String instrumentName;

  AbstractInstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    this.instrumentName = name;
    this.description = description;
    this.unit = unit;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
  }

  protected abstract BuilderT getThis();

  public BuilderT setUnit(String unit) {
    if (!ValidationUtil.checkValidInstrumentUnit(
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

  private InstrumentDescriptor makeDescriptor(InstrumentType type, InstrumentValueType valueType) {
    return InstrumentDescriptor.create(instrumentName, description, unit, type, valueType);
  }

  protected <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState, meterSharedState, instrumentName, description, unit);
  }

  final <I extends AbstractInstrument> I buildSynchronousInstrument(
      InstrumentType type,
      InstrumentValueType valueType,
      BiFunction<InstrumentDescriptor, WriteableMetricStorage, I> instrumentFactory) {
    InstrumentDescriptor descriptor = makeDescriptor(type, valueType);
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.apply(descriptor, storage);
  }

  final CallbackRegistration registerDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement =
        buildObservableMeasurement(type, InstrumentValueType.DOUBLE);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return callbackRegistration;
  }

  final CallbackRegistration registerLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement =
        buildObservableMeasurement(type, InstrumentValueType.LONG);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return callbackRegistration;
  }

  final SdkObservableMeasurement buildObservableMeasurement(
      InstrumentType type, InstrumentValueType valueType) {
    return meterSharedState.registerObservableMeasurement(makeDescriptor(type, valueType));
  }

  @FunctionalInterface
  protected interface SwapBuilder<T> {
    T newBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name,
        String description,
        String unit);
  }
}
