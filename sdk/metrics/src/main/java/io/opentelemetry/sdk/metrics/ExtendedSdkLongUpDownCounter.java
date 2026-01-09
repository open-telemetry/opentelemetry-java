/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounterBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkLongUpDownCounter extends SdkLongUpDownCounter
    implements ExtendedLongUpDownCounter {

  private ExtendedSdkLongUpDownCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
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
