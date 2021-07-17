/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.state.WriteableInstrumentStorage;
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
public abstract class AbstractInstrumentBuilder<BuilderT extends AbstractInstrumentBuilder<?>> {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final String instrumentName;
  private String description;
  private String unit;

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
    this.unit = unit;
    return getThis();
  }

  public BuilderT setDescription(String description) {
    this.description = description;
    return getThis();
  }

  @FunctionalInterface
  protected static interface SwapBuilder<T> {
    T newBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit);
  }

  private InstrumentDescriptor makeDescriptor(InstrumentType type, InstrumentValueType valueType) {
    return InstrumentDescriptor.create(instrumentName, description, unit, type, valueType);
  }

  protected <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState, meterSharedState, instrumentName, description, unit);
  }

  protected WriteableInstrumentStorage makeSynchronousStorage(
      InstrumentType type, InstrumentValueType valueType) {
    InstrumentDescriptor descriptor = makeDescriptor(type, valueType);
    return meterSharedState
        .getInstrumentStorageRegistry()
        .register(
            descriptor,
            () ->
                meterProviderSharedState
                    .getMeasurementProcessor()
                    .createStorage(descriptor, meterProviderSharedState, meterSharedState));
  }

  protected <T extends ObservableMeasurement> void registerAsychronousStorage(
      InstrumentType type, InstrumentValueType valueType, Consumer<T> callback) {
    InstrumentDescriptor descriptor = makeDescriptor(type, valueType);
    meterSharedState
        .getInstrumentStorageRegistry()
        .register(
            descriptor,
            () ->
                meterProviderSharedState
                    .getMeasurementProcessor()
                    .createAsynchronousStorage(
                        descriptor, meterProviderSharedState, meterSharedState, callback));
  }
}
