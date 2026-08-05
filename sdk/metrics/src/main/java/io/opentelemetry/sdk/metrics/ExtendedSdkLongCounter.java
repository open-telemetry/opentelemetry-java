/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkLongCounter extends SdkLongCounter
    implements ExtendedLongCounter, BoundLongCounter {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the add() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  private ExtendedSdkLongCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkLongCounter(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundLongCounter bind(Attributes attributes) {
    return new ExtendedSdkLongCounter(getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void add(long value) {
    add(value, Context.current());
  }

  @Override
  public void add(long value, Context context) {
    if (!validateNonNegative(value)) {
      return;
    }
    if (boundHandle != null) {
      boundHandle.recordLong(value, context);
    } else {
      storage.recordLong(value, Attributes.empty(), context);
    }
  }

  static final class ExtendedSdkLongCounterBuilder extends SdkLongCounterBuilder
      implements ExtendedLongCounterBuilder {

    ExtendedSdkLongCounterBuilder(SdkMeter sdkMeter, String name) {
      super(sdkMeter, name);
    }

    @Override
    public ExtendedSdkLongCounter build() {
      return builder.buildSynchronousInstrument(ExtendedSdkLongCounter::new);
    }

    @Override
    public ExtendedDoubleCounterBuilder ofDoubles() {
      return builder.swapBuilder(ExtendedSdkDoubleCounter.ExtendedSdkDoubleCounterBuilder::new);
    }

    @Override
    public ExtendedLongCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
