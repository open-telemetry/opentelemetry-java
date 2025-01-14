/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkLongGauge extends SdkLongGauge implements ExtendedLongGauge {

  private ExtendedSdkLongGauge(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  static final class ExtendedSdkLongGaugeBuilder extends SdkLongGaugeBuilder
      implements ExtendedLongGaugeBuilder {

    ExtendedSdkLongGaugeBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      super(sdkMeter, name, description, unit, adviceBuilder);
    }

    @Override
    public ExtendedSdkLongGauge build() {
      return builder.buildSynchronousInstrument(ExtendedSdkLongGauge::new);
    }

    @Override
    public ExtendedLongGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
