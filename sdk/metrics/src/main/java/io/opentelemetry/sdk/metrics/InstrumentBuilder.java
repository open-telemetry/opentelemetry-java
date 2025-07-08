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
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Helper to make implementing builders easier. */
final class InstrumentBuilder {

  private final String name;
  private final SdkMeter sdkMeter;
  private final InstrumentValueType valueType;
  private InstrumentType type;
  private Advice.AdviceBuilder adviceBuilder = Advice.builder();
  private String description = "";
  private String unit = "";

  InstrumentBuilder(
      String name, InstrumentType type, InstrumentValueType valueType, SdkMeter sdkMeter) {
    this.name = name;
    this.type = type;
    this.valueType = valueType;
    this.sdkMeter = sdkMeter;
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
    return swapper.newBuilder(sdkMeter, name, description, unit, adviceBuilder);
  }

  @FunctionalInterface
  interface SynchronousInstrumentConstructor<I extends AbstractInstrument> {

    I createInstrument(
        InstrumentDescriptor instrumentDescriptor,
        SdkMeter sdkMeter,
        WriteableMetricStorage storage);
  }

  <I extends AbstractInstrument> I buildSynchronousInstrument(
      SynchronousInstrumentConstructor<I> instrumentFactory) {
    InstrumentDescriptor descriptor = newDescriptor();
    WriteableMetricStorage storage = sdkMeter.registerSynchronousMetricStorage(descriptor);
    return instrumentFactory.createInstrument(descriptor, sdkMeter, storage);
  }

  SdkObservableInstrument buildDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    sdkMeter.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(sdkMeter, callbackRegistration);
  }

  SdkObservableInstrument buildLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    SdkObservableMeasurement sdkObservableMeasurement = buildObservableMeasurement(type);
    Runnable runnable = () -> updater.accept(sdkObservableMeasurement);
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(Collections.singletonList(sdkObservableMeasurement), runnable);
    sdkMeter.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(sdkMeter, callbackRegistration);
  }

  SdkObservableMeasurement buildObservableMeasurement(InstrumentType type) {
    this.type = type;
    InstrumentDescriptor descriptor = newDescriptor();
    return sdkMeter.registerObservableMeasurement(descriptor);
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
  interface SwapBuilder<T> {
    T newBuilder(
        SdkMeter sdkMeter,
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
