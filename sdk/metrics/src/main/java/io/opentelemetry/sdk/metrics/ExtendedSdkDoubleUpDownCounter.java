/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkDoubleUpDownCounter extends SdkDoubleUpDownCounter
    implements ExtendedDoubleUpDownCounter, BoundDoubleUpDownCounter {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the add() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  private ExtendedSdkDoubleUpDownCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkDoubleUpDownCounter(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundDoubleUpDownCounter bind(Attributes attributes) {
    return new ExtendedSdkDoubleUpDownCounter(
        getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void add(double value) {
    add(value, Context.current());
  }

  @Override
  public void add(double value, Context context) {
    if (boundHandle != null) {
      boundHandle.recordDouble(value, context);
    } else {
      storage.recordDouble(value, Attributes.empty(), context);
    }
  }

  static final class ExtendedSdkDoubleUpDownCounterBuilder extends SdkDoubleUpDownCounterBuilder
      implements ExtendedDoubleUpDownCounterBuilder {

    ExtendedSdkDoubleUpDownCounterBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      super(sdkMeter, name, description, unit, adviceBuilder);
    }

    @Override
    public ExtendedDoubleUpDownCounter build() {
      return builder.buildSynchronousInstrument(ExtendedSdkDoubleUpDownCounter::new);
    }

    @Override
    public ExtendedDoubleUpDownCounterBuilder setAttributesAdvice(
        List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
