/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkLongCounter extends SdkLongCounter implements ExtendedLongCounter {

  private ExtendedSdkLongCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
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
