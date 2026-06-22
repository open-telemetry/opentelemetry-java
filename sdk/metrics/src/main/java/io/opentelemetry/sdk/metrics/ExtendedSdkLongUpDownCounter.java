/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounterBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkLongUpDownCounter extends SdkLongUpDownCounter
    implements ExtendedLongUpDownCounter, BoundLongUpDownCounter {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the add() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  private ExtendedSdkLongUpDownCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkLongUpDownCounter(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundLongUpDownCounter bind(Attributes attributes) {
    return new ExtendedSdkLongUpDownCounter(
        getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void add(long value) {
    add(value, Context.current());
  }

  @Override
  public void add(long value, Context context) {
    if (boundHandle != null) {
      boundHandle.recordLong(value, context);
    } else {
      storage.recordLong(value, Attributes.empty(), context);
    }
  }

  static final class ExtendedSdkLongUpDownCounterBuilder extends SdkLongUpDownCounterBuilder
      implements ExtendedLongUpDownCounterBuilder {

    ExtendedSdkLongUpDownCounterBuilder(SdkMeter sdkMeter, String name) {
      super(sdkMeter, name);
    }

    @Override
    public ExtendedLongUpDownCounter build() {
      return builder.buildSynchronousInstrument(ExtendedSdkLongUpDownCounter::new);
    }

    @Override
    public ExtendedDoubleUpDownCounterBuilder ofDoubles() {
      return builder.swapBuilder(
          ExtendedSdkDoubleUpDownCounter.ExtendedSdkDoubleUpDownCounterBuilder::new);
    }

    @Override
    public ExtendedLongUpDownCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
