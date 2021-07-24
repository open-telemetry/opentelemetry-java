/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

import java.util.function.BiFunction;
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
        MeterSharedState meterSharedState,
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

  final <I extends AbstractInstrument> I buildSynchronousInstrument(
    InstrumentType type, InstrumentValueType valueType,
    BiFunction<InstrumentDescriptor, SynchronousInstrumentAccumulator<?>, I> instrumentFactory) {
     InstrumentDescriptor descriptor = makeDescriptor(type, valueType);
     return meterSharedState.getInstrumentRegistry().register(instrumentFactory.apply(descriptor, 
     SynchronousInstrumentAccumulator.create(meterProviderSharedState, meterSharedState, descriptor)));
    }
  

  final <I extends AbstractInstrument> I buildDoubleAsynchronousInstrument(
    InstrumentType type,
    Consumer<ObservableDoubleMeasurement> updater,
    BiFunction<InstrumentDescriptor, AsynchronousInstrumentAccumulator, I> instrumentFactory
  ) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.DOUBLE);
    return meterSharedState.getInstrumentRegistry()
    .register(instrumentFactory.apply(descriptor, 
      AsynchronousInstrumentAccumulator.doubleAsynchronousAccumulator(meterProviderSharedState, meterSharedState, descriptor, updater)));
  }

  final <I extends AbstractInstrument> I buildLongAsynchronousInstrument(
    InstrumentType type,
    Consumer<ObservableLongMeasurement> updater,
    BiFunction<InstrumentDescriptor, AsynchronousInstrumentAccumulator, I> instrumentFactory
  ) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.DOUBLE);
    return meterSharedState.getInstrumentRegistry()
    .register(instrumentFactory.apply(descriptor, 
      AsynchronousInstrumentAccumulator.longAsynchronousAccumulator(meterProviderSharedState, meterSharedState, descriptor, updater)));
  }
}