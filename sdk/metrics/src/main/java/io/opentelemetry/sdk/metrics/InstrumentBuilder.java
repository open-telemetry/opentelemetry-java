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
import io.opentelemetry.sdk.metrics.internal.descriptor.MutableInstrumentDescriptor;
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

  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final MutableInstrumentDescriptor descriptor;

  InstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      MutableInstrumentDescriptor descriptor) {
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
    this.descriptor = descriptor;
  }

  public void setUnit(String unit) {
    descriptor.setUnit(unit);
    ;
  }

  public void setDescription(String description) {
    descriptor.setDescription(description);
  }

  <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState,
        meterSharedState,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        descriptor.getAdviceBuilder());
  }

  <I extends AbstractInstrument> I buildSynchronousInstrument(
      BiFunction<InstrumentDescriptor, WriteableMetricStorage, I> instrumentFactory) {
    InstrumentDescriptor descriptor = this.descriptor.toImmutable();
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.apply(descriptor, storage);
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
    this.descriptor.setType(type);
    InstrumentDescriptor descriptor = this.descriptor.toImmutable();
    return meterSharedState.registerObservableMeasurement(descriptor);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass().getSimpleName());
  }

  String toStringHelper(String className) {
    return className + "{descriptor=" + descriptor.toImmutable() + "}";
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
    descriptor.setAdviceAttributes(attributes);
  }

  void setExplicitBucketBoundaries(List<Double> bucketBoundaries) {
    descriptor.setExplicitBucketBoundaries(bucketBoundaries);
  }
}
