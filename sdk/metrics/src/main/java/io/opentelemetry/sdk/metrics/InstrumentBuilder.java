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
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
final class InstrumentBuilder {

  private final String name;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final InstrumentValueType valueType;
  private InstrumentType type;
  private Advice.AdviceBuilder adviceBuilder = Advice.builder();
  private String description = "";
  private String unit = "";

  InstrumentBuilder(
      String name,
      InstrumentType type,
      InstrumentValueType valueType,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    this.name = name;
    this.type = type;
    this.valueType = valueType;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
  }

  InstrumentBuilder setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  InstrumentBuilder setAdviceBuilder(Advice.AdviceBuilder adviceBuilder) {
    this.adviceBuilder = adviceBuilder;
    return this;
  }

  InstrumentBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState, meterSharedState, name, description, unit, adviceBuilder);
  }

  @FunctionalInterface
  interface SynchronousInstrumentConstructor<I extends AbstractInstrument> {

    I createInstrument(
        InstrumentDescriptor instrumentDescriptor,
        MeterSharedState meterSharedState,
        WriteableMetricStorage storage);
  }

  <I extends AbstractInstrument> I buildSynchronousInstrument(
      SynchronousInstrumentConstructor<I> instrumentFactory) {
    InstrumentDescriptor descriptor = newDescriptor();
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.createInstrument(descriptor, meterSharedState, storage);
  }

  SdkObservableInstrument buildDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  SdkObservableInstrument buildLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  SdkObservableMeasurement buildObservableMeasurement(InstrumentType type) {
    this.type = type;
    InstrumentDescriptor descriptor = newDescriptor();
    return meterSharedState.registerObservableMeasurement(descriptor);
  }

  private InstrumentDescriptor newDescriptor() {
    return InstrumentDescriptor.create(
        name, description, unit, type, valueType, adviceBuilder.build());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass().getSimpleName());
  }

  String toStringHelper(String className) {
    return className + "{descriptor=" + newDescriptor() + "}";
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

  void setAdviceAttributes(List<AttributeKey<?>> attributes) {
    adviceBuilder.setAttributes(attributes);
  }

  void setExplicitBucketBoundaries(List<Double> bucketBoundaries) {
    adviceBuilder.setExplicitBucketBoundaries(bucketBoundaries);
  }
}
