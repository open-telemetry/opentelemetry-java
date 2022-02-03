/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
public abstract class AbstractInstrumentBuilder<BuilderT extends AbstractInstrumentBuilder<?>> {

  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private String description;
  private String unit;

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
    this.unit = unit;
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

  final List<AsynchronousMetricStorage<?, ObservableDoubleMeasurement>>
      registerDoubleAsynchronousInstrument(
          InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.DOUBLE);
    return meterSharedState.registerDoubleAsynchronousInstrument(
        descriptor, meterProviderSharedState, updater);
  }

  final List<AsynchronousMetricStorage<?, ObservableLongMeasurement>>
      registerLongAsynchronousInstrument(
          InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.LONG);
    return meterSharedState.registerLongAsynchronousInstrument(
        descriptor, meterProviderSharedState, updater);
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
