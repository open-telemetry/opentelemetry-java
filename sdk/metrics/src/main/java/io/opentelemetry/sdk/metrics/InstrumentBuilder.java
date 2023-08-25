/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
final class InstrumentBuilder {

  private static final String DEFAULT_UNIT = "";

  private final MeterProviderSharedState meterProviderSharedState;
  private final InstrumentType type;
  private final InstrumentValueType valueType;
  private String description;
  private String unit;

  private final MeterSharedState meterSharedState;
  private final String instrumentName;
  private final Advice.AdviceBuilder adviceBuilder;

  InstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentType type,
      InstrumentValueType valueType,
      String name) {
    this(meterProviderSharedState, meterSharedState,
        type, valueType, name, "", DEFAULT_UNIT, Advice.builder());
  }

  InstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentType type,
      InstrumentValueType valueType,
      String name,
      String description,
      String unit,
      Advice.AdviceBuilder adviceBuilder) {
    this.type = type;
    this.valueType = valueType;
    this.instrumentName = name;
    this.description = description;
    this.unit = unit;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
    this.adviceBuilder = adviceBuilder;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState,
        meterSharedState,
        instrumentName,
        description,
        unit,
        adviceBuilder);
  }

  <I extends AbstractInstrument> I buildSynchronousInstrument(
      BiFunction<InstrumentDescriptor, WriteableMetricStorage, I> instrumentFactory) {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            instrumentName, description, unit, type, valueType, adviceBuilder.build());
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.apply(descriptor, storage);
  }

  SdkObservableInstrument registerDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  SdkObservableInstrument registerLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  SdkObservableMeasurement buildObservableMeasurement(InstrumentType type) {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            instrumentName, description, unit, type, valueType, adviceBuilder.build());
    return meterSharedState.registerObservableMeasurement(descriptor);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass().getSimpleName());
  }

  String toStringHelper(String className) {
    return className
        + "{descriptor="
        + InstrumentDescriptor.create(
            instrumentName, description, unit, type, valueType, adviceBuilder.build())
        + "}";
  }

  public void setAdviceAttributes(List<AttributeKey<?>> attributes) {
    adviceBuilder.setAttributes(attributes);
  }

  public void setExplicitBucketBoundaries(List<Double> bucketBoundaries) {
    adviceBuilder.setExplicitBucketBoundaries(bucketBoundaries);
  }

  @FunctionalInterface
  protected interface SwapBuilder<T> {
    T newBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder);
  }
}
